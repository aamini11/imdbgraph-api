package org.aria.imdbgraph.scrapper;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Map.entry;
import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile;
import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile.*;

/**
 * DatabaseUpdater is a utility class whose responsibility is to fetch the latest
 * flat files from IMDB and update the databases with that data. All the update
 * operations are performed in bulk since IMDB only updates their files once a day.
 * That makes retrieving data in real-time from IMDB impossible, unfortunately.
 */
@Service
public class DatabaseUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUpdater.class);

    private final DataSource dataSource;
    private final ImdbFileService fileService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor to initialize the {@code DatabaseUpdater} with all its
     * required dependencies
     *
     * @param dataSource     The {@code DataSource} object representing the
     *                       database that you want to be updated.
     * @param fileService The {@code FileDownloader} class responsible for
     *                       downloading and preparing all the files which will
     *                       be read into the database.
     *                       <p>
     *                       Note: This dependency can be mocked for unit
     *                       testing so that you don't have to download a file
     *                       every time you test this class.
     */
    @Autowired
    DatabaseUpdater(DataSource dataSource,
                    ImdbFileService fileService) {
        this.dataSource = dataSource;
        this.fileService = fileService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Method that will fetch the latest files from IMDB and updating the database
     * with that data.
     *
     * @throws DailyUpdateError This method will throw a file loading error
     * if it encounters and IO/SQL errors that prevent it from performing the
     * full update.
     */
    @Transactional
    public void updateDatabase() {
        populateAllTempTables();
        updateShows();
        updateEpisodes();
        // Refresh cache which contains all the shows with at least one
        // episode rating
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW imdb.valid_show;");
    }

    /**
     * Method that's responsible for downloading all the files from IMDB and
     * loading them into temporary tables in the database. Once these files
     * are copied into the temp tables, they will soon be reformatted and loaded
     * into the real tables.
     */
    private void populateAllTempTables() {
        jdbcTemplate.execute("" +
                "CREATE TEMPORARY TABLE temp_title\n" +
                "(\n" +
                "    imdb_id         VARCHAR(10) PRIMARY KEY,\n" +
                "    title_type      TEXT,\n" +
                "    primary_title   TEXT,\n" +
                "    original_title  TEXT,\n" +
                "    is_adult        BOOLEAN,\n" +
                "    start_year      CHAR(4),\n" +
                "    end_year        CHAR(4),\n" +
                "    runtime_minutes INT,\n" +
                "    genres          TEXT\n" +
                ") ON COMMIT DROP;\n " +

                "CREATE TEMPORARY TABLE temp_episode\n" +
                "(\n" +
                "    episode_id  VARCHAR(10) PRIMARY KEY,\n" +
                "    show_id     VARCHAR(10),\n" +
                "    season_num  INT,\n" +
                "    episode_num INT\n" +
                ") ON COMMIT DROP;\n" +

                "CREATE TEMPORARY TABLE temp_ratings\n" +
                "(\n" +
                "    imdb_id     VARCHAR(10),\n" +
                "    imdb_rating DOUBLE PRECISION,\n" +
                "    num_votes   INT\n" +
                ") ON COMMIT DROP;");

        Map<ImdbFile, String> fileToTableMapping = Map.ofEntries(
                entry(TITLES_FILE, "temp_title"),
                entry(EPISODES_FILE, "temp_episode"),
                entry(RATINGS_FILE, "temp_ratings")
        );

        Set<ImdbFile> filesToDownload = fileToTableMapping.keySet();
        Map<ImdbFile, Path> downloadedFiles = fileService.download(filesToDownload);
        fileService.archive();
        for (Entry<ImdbFile, Path> e : downloadedFiles.entrySet()) {
            ImdbFile fileToLoad = e.getKey();
            String tableName = fileToTableMapping.get(fileToLoad);
            Path pathOfFileToLoad = e.getValue();

            populate(tableName, pathOfFileToLoad);
        }
    }

    /**
     * Helper method that will use Postgres's COPY command to copy data from
     * a file into a table.
     * @param tableName Name of the Postgres table you want to fill with data.
     * @param fileToLoad Path of file you want to load data from.
     */
    private void populate(String tableName, Path fileToLoad) {
        try (BufferedReader br = Files.newBufferedReader(fileToLoad)) {
            BaseConnection postgresConnection = DataSourceUtils.getConnection(dataSource)
                    .unwrap(BaseConnection.class);

            CopyManager copier = new CopyManager(postgresConnection);
            //language=SQL
            String command = "" +
                    "COPY %s\n" +
                    "FROM STDIN\n" +
                    "WITH (DELIMITER '\t');";

            String header = br.readLine();
            if (header == null) throw new DailyUpdateError("Empty file received");
            copier.copyIn(String.format(command, tableName), br);

            logger.info("{} successfully transferred to table {}", fileToLoad, tableName);
        } catch (SQLException | IOException exception) {
            throw new DailyUpdateError(exception);
        }
    }

    /**
     * Updates show table using freshly loaded data from temp tables. The method
     * also reformats/transforms that data before using it for updates.
     */
    private void updateShows() {
        jdbcTemplate.execute("" +
                "INSERT INTO imdb.show(imdb_id,\n" +
                "                      primary_title,\n" +
                "                      start_year,\n" +
                "                      end_year,\n" +
                "                      imdb_rating,\n" +
                "                      num_votes)\n" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       COALESCE(imdb_rating, 0.0), \n" +
                "       COALESCE(num_votes, 0)\n" +
                "FROM temp_title\n" +
                "         LEFT JOIN temp_ratings USING (imdb_id)\n" +
                "WHERE title_type IN ('tvSeries', 'tvShort', 'tvSpecial', 'tvMiniSeries')\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "    SET primary_title = excluded.primary_title,\n" +
                "        start_year    = excluded.start_year,\n" +
                "        end_year      = excluded.end_year,\n" +
                "        imdb_rating   = excluded.imdb_rating,\n" +
                "        num_votes     = excluded.num_votes;");
        logger.info("Shows successfully updated");
    }

    /**
     * Updates episode table using freshly loaded data from temp tables. The
     * method also reformats/transforms that data before using it for updates.
     */
    private void updateEpisodes() {
        jdbcTemplate.execute("" +
                "INSERT INTO imdb.episode(show_id,\n" +
                "                         episode_id,\n" +
                "                         episode_title,\n" +
                "                         season_num,\n" +
                "                         episode_num,\n" +
                "                         imdb_rating,\n" +
                "                         num_votes)\n" +
                "SELECT show_id,\n" +
                "       episode_id,\n" +
                "       primary_title,\n" +
                "       season_num,\n" +
                "       episode_num,\n" +
                "       COALESCE(imdb_rating, 0.0),\n" +
                "       COALESCE(num_votes, 0)\n" +
                "FROM temp_episode\n" +
                "         LEFT JOIN temp_title ON (episode_id = imdb_id)\n" +
                "         LEFT JOIN temp_ratings USING (imdb_id)\n" +
                "WHERE show_id IN (SELECT imdb_id FROM imdb.show)\n" +
                "  AND season_num >= 0\n" +
                "  AND episode_num >= 0\n" +
                "ON CONFLICT (episode_id) DO UPDATE\n" +
                "    SET show_id       = excluded.show_id,\n" +
                "        episode_title = excluded.episode_title,\n" +
                "        season_num    = excluded.season_num,\n" +
                "        episode_num   = excluded.episode_num,\n" +
                "        imdb_rating   = excluded.imdb_rating,\n" +
                "        num_votes     = excluded.num_votes;");
        logger.info("Episodes successfully updated");
    }

    /**
     * Exception to indicate any issues when performing daily database updates
     */
    static class DailyUpdateError extends RuntimeException {
        DailyUpdateError(String message) {
            super(message);
        }

        DailyUpdateError(Throwable cause) {
            super(cause);
        }
    }
}
