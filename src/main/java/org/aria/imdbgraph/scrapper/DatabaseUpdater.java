package org.aria.imdbgraph.scrapper;

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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile;
import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile.*;

/**
 * Utility class whose responsible for fetching the latest flat files from IMDB
 * and update the databases with that data. All the update operations are
 * performed in bulk since IMDB only updates their files once a day. That makes
 * retrieving data in real-time from IMDB impossible, unfortunately.
 * <p>
 * Daily update are done at 8:00 AM UTC.
 * <p>
 * If any error occurs when trying to load the IMDB files into the database,
 * whether from malformed files or just general Database/IO errors, an error
 * message will be logged and all files from that session will be archived. The
 * archived files can be found under a directory called archive in the data
 * directory that is specified by the property 'imdbgraph.data.directory' in the
 * spring application.properties file.
 */
@Repository
@EnableScheduling
public class DatabaseUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUpdater.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ImdbFileService fileService;
    private final FileArchiver fileArchiver;

    @Autowired
    DatabaseUpdater(JdbcTemplate jdbcTemplate,
                    ImdbFileService fileService,
                    FileArchiver fileArchiver) {
        this.fileService = fileService;
        this.jdbcTemplate = jdbcTemplate;
        this.fileArchiver = fileArchiver;
        this.dataSource = jdbcTemplate.getDataSource();
    }

    /**
     * Main method that is scheduled to fetch the latest files from IMDB and
     * update the database with that data.
     */
    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void updateDatabase() throws ImdbFileParsingError {
        populateAllTempTables();
        updateShows();
        updateEpisodes();
        // Refresh cache which contains all the shows with at least one
        // episode rating
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW imdb.valid_show;");
    }

    /**
     * Method that's responsible for downloading all the files from IMDB and
     * loading them into temporary tables in the database. Once these files are
     * copied into the temp tables, they will soon be reformatted and loaded
     * into the real tables. And if successfully loaded, the file will be
     * deleted immediately afterwards
     */
    private void populateAllTempTables() throws ImdbFileParsingError {
        jdbcTemplate.execute("""
                CREATE TEMPORARY TABLE temp_title
                (
                    imdb_id         VARCHAR(10) PRIMARY KEY,
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
                    episode_id  VARCHAR(10) PRIMARY KEY,
                    show_id     VARCHAR(10),
                    season_num  INT,
                    episode_num INT
                ) ON COMMIT DROP;

                CREATE TEMPORARY TABLE temp_ratings
                (
                    imdb_id     VARCHAR(10),
                    imdb_rating DOUBLE PRECISION,
                    num_votes   INT
                ) ON COMMIT DROP;
                """);

        Map<ImdbFile, String> fileToTableMapping = Map.ofEntries(
                entry(TITLES_FILE, "temp_title"),
                entry(EPISODES_FILE, "temp_episode"),
                entry(RATINGS_FILE, "temp_ratings")
        );

        Set<ImdbFile> filesToDownload = fileToTableMapping.keySet();
        Map<ImdbFile, Path> allDownloadedFiles = new EnumMap<>(ImdbFile.class);
        for (ImdbFile imdbFile : filesToDownload) {
            Path downloadLocation = fileService.download(imdbFile);
            allDownloadedFiles.put(imdbFile, downloadLocation);
        }

        for (Map.Entry<ImdbFile, Path> entry : allDownloadedFiles.entrySet()) {
            ImdbFile fileToLoad = entry.getKey();
            Path pathOfFileToLoad = entry.getValue();

            String tableName = fileToTableMapping.get(fileToLoad);
            populate(tableName, pathOfFileToLoad);
            try {
                Files.delete(pathOfFileToLoad); // clean up file after successful import
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Helper method that will use Postgres' COPY command to copy data from a
     * file into a table.
     *
     * @param tableName  Name of the Postgres table you want to fill with data.
     * @param fileToLoad Path of file you want to load data from.
     */
    private void populate(String tableName, Path fileToLoad) throws ImdbFileParsingError {
        try (BufferedReader br = Files.newBufferedReader(fileToLoad)) {
            PgConnection postgresConnection = DataSourceUtils.getConnection(dataSource)
                    .unwrap(PgConnection.class);

            CopyManager copier = new CopyManager(postgresConnection);
            //language=SQL
            String command = """
                    COPY %s
                    FROM STDIN
                    WITH (DELIMITER '\t');
                    """;

            String header = br.readLine();
            if (header == null)
                throw new FileNotFoundException(fileToLoad.toString());
            copier.copyIn(String.format(command, tableName), br);

            logger.info("{} successfully transferred to table {}", fileToLoad, tableName);
        } catch (SQLException | IOException exception) {
            fileArchiver.archive(fileToLoad);
            throw new ImdbFileParsingError(exception, fileToLoad);
        }
    }

    /**
     * Updates show table using freshly loaded data from temp tables. The method
     * also reformats/transforms that data before using it for updates.
     */
    private void updateShows() {
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
    }

    /**
     * Updates episode table using freshly loaded data from temp tables. The
     * method also reformats/transforms that data before using it for updates.
     */
    private void updateEpisodes() {
        jdbcTemplate.execute("""
                INSERT INTO imdb.episode(show_id,
                                         episode_id,
                                         episode_title,
                                         season_num,
                                         episode_num,
                                         imdb_rating,
                                         num_votes)
                SELECT show_id,
                       episode_id,
                       primary_title,
                       season_num,
                       episode_num,
                       COALESCE(imdb_rating, 0.0),
                       COALESCE(num_votes, 0)
                FROM temp_episode
                         LEFT JOIN temp_title ON (episode_id = imdb_id)
                         LEFT JOIN temp_ratings USING (imdb_id)
                WHERE show_id IN (SELECT imdb_id FROM imdb.show)
                  AND season_num >= 0
                  AND episode_num >= 0
                ON CONFLICT (episode_id) DO UPDATE
                    SET show_id       = excluded.show_id,
                        episode_title = excluded.episode_title,
                        season_num    = excluded.season_num,
                        episode_num   = excluded.episode_num,
                        imdb_rating   = excluded.imdb_rating,
                        num_votes     = excluded.num_votes;
                """);
        logger.info("Episodes successfully updated");
    }

    static class ImdbFileParsingError extends Exception {
        @Serial
        private static final long serialVersionUID = 8382725531069054752L;

        final transient Path failedFile;

        public ImdbFileParsingError(Throwable cause, Path failedFile) {
            super("Failed to load file from: " + failedFile, cause);
            this.failedFile = failedFile;
        }
    }
}
