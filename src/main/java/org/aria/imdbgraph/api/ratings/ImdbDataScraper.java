package org.aria.imdbgraph.api.ratings;

import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.String.format;
import static org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile;
import static org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile.*;

/**
 * IMDB has no free API to use to fetch their ratings data directly. Instead,
 * they release all their data in text files once a day. This class downloads
 * those files daily and updates an internal database with all the new data.
 */
@Repository
@EnableScheduling
public class ImdbDataScraper {

    private static final Logger logger = LoggerFactory.getLogger(ImdbDataScraper.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ImdbFileDownloader imdbFileDownloader;

    @Autowired
    public ImdbDataScraper(
            JdbcTemplate jdbcTemplate,
            ImdbFileDownloader imdbFileDownloader
    ) {
        this.imdbFileDownloader = imdbFileDownloader;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = jdbcTemplate.getDataSource();
    }

    /**
     * Main method that downloads the latest files from IMDB and updating our
     * internal database with the latest data.
     * <p>
     * Runs daily at 8:00 AM UTC.
     *
     * @throws ImdbFileParsingException If any error occurs when trying to load
     *                                  the IMDB files into the database.
     */
    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void updateDatabase() throws ImdbFileParsingException {
        update();
    }

    /*
     * Download files and store them in temp tables using the Postgres copy
     * command. This is the most efficient way to bulk update data from files
     * into a postgres database. Once all data is loaded into temp tables,
     * update all the real tables with data from the temp tables.
     *
     * https://stackoverflow.com/a/17267423/6310030
     * https://www.postgresql.org/docs/current/populate.html
     * https://dba.stackexchange.com/questions/41059/optimizing-bulk-update-performance-in-postgresql
     */
    public void update() {
        jdbcTemplate.execute("""
                CREATE TEMPORARY TABLE temp_title
                (
                    imdb_id         VARCHAR(10),
                    title_type      TEXT,
                    primary_title   TEXT,
                    original_title  TEXT,
                    is_adult        BOOLEAN,
                    start_year      CHAR(4),
                    end_year        CHAR(4),
                    runtime_minutes INT,
                    genres          TEXT
                ) ON COMMIT DROP;
                
                CREATE TEMPORARY TABLE temp_episode
                (
                    episode_id  VARCHAR(10),
                    show_id     VARCHAR(10),
                    season_num  INT,
                    episode_num INT
                ) ON COMMIT DROP;
                
                CREATE TEMPORARY TABLE temp_ratings
                (
                    imdb_id     VARCHAR(10) PRIMARY KEY,
                    imdb_rating DOUBLE PRECISION,
                    num_votes   INT
                ) ON COMMIT DROP;
                """);

        // Copy from files to temp tables
        Map<ImdbFile, String> tables = Map.of(
                TITLES, "temp_title",
                EPISODES, "temp_episode",
                RATINGS, "temp_ratings"
        );
        for (Entry<ImdbFile, String> e : tables.entrySet()) {
            ImdbFile file = e.getKey();
            String table = e.getValue();

            Path downloadedFile = imdbFileDownloader.download(file);
            copy(downloadedFile, table);
        }

        // Updates show table using new data from temp tables.
        //noinspection SqlResolve,SqlCheckUsingColumns
        jdbcTemplate.execute("""
                INSERT INTO imdb.show(imdb_id,
                                      primary_title,
                                      start_year,
                                      end_year,
                                      imdb_rating,
                                      num_votes)
                SELECT imdb_id,
                       primary_title,
                       start_year,
                       end_year,
                       COALESCE(imdb_rating, 0.0),
                       COALESCE(num_votes, 0)
                FROM temp_title
                         LEFT JOIN temp_ratings USING (imdb_id)
                WHERE title_type IN ('tvSeries', 'tvShort', 'tvSpecial', 'tvMiniSeries')
                ON CONFLICT (imdb_id) DO UPDATE
                    SET primary_title = excluded.primary_title,
                        start_year    = excluded.start_year,
                        end_year      = excluded.end_year,
                        imdb_rating   = excluded.imdb_rating,
                        num_votes     = excluded.num_votes;
                """);
        logger.info("Shows successfully updated");

        // Updates episode table using new data from temp tables.
        //noinspection SqlResolve,SqlCheckUsingColumns
        jdbcTemplate.execute("""
                DROP TABLE IF EXISTS imdb.episode_new;
                
                CREATE TABLE imdb.episode_new AS
                SELECT show_id,
                       episode_id,
                       primary_title as episode_title,
                       season_num,
                       episode_num,
                       COALESCE(imdb_rating, 0.0) as imdb_rating,
                       COALESCE(num_votes, 0) as num_votes
                FROM temp_episode
                         LEFT JOIN temp_title ON (episode_id = imdb_id)
                         LEFT JOIN temp_ratings USING (imdb_id)
                WHERE show_id IN (SELECT imdb_id FROM imdb.show)
                  AND season_num >= 0
                  AND episode_num >= 0;
                
                ALTER TABLE imdb.episode_new ADD PRIMARY KEY (episode_id);
                ALTER TABLE imdb.episode_new ADD FOREIGN KEY (show_id) REFERENCES imdb.show(imdb_id);
                CREATE INDEX ON imdb.episode_new (show_id);
                
                ALTER TABLE imdb.episode_new ALTER COLUMN show_id SET NOT NULL;
                ALTER TABLE imdb.episode_new ALTER COLUMN episode_id SET NOT NULL;
                ALTER TABLE imdb.episode_new ALTER COLUMN season_num SET NOT NULL;
                ALTER TABLE imdb.episode_new ALTER COLUMN episode_num SET NOT NULL;
                ALTER TABLE imdb.episode_new ALTER COLUMN imdb_rating SET NOT NULL;
                ALTER TABLE imdb.episode_new ALTER COLUMN num_votes SET NOT NULL;
                
                DROP TABLE imdb.episode;
                ALTER TABLE imdb.episode_new RENAME TO episode;
                """);
        logger.info("Episodes successfully updated");
    }

    /**
     * Use Postgres COPY command to copy data from a file into a table.
     */
    private void copy(Path path, String table) {
        try (BufferedReader f = Files.newBufferedReader(path)) {
            PgConnection connection = DataSourceUtils.getConnection(dataSource)
                    .unwrap(PgConnection.class);

            String header = f.readLine();
            if (header == null) {
                throw new FileNotFoundException(path.toString());
            }
            CopyManager copier = new CopyManager(connection);
            //language=SQL
            String cmd = format("COPY %s FROM STDIN WITH (DELIMITER '\t');", table);
            copier.copyIn(cmd, f);

            // Clean up
            logger.info("Successfully transferred {} to table {}", path, table);
            Files.delete(path);
        } catch (SQLException | IOException exception) {
            throw new ImdbFileParsingException(exception, path);
        }
    }

    public static class ImdbFileParsingException extends RuntimeException {
        public ImdbFileParsingException(Throwable cause, Path failedFile) {
            super("Failed to load file from: " + failedFile, cause);
        }
    }
}
