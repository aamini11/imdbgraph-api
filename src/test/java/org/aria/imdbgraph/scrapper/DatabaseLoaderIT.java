package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.DatabaseUpdater.ImdbFileParsingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import static org.aria.imdbgraph.scrapper.ImdbFileDownloader.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseLoaderIT {

    private static final Path SAMPLE_FILES_DIR = Paths.get("src/test/resources/samples-files");
    private static Path INPUT_DIR;
    private static Path ARCHIVE_DIR;

    @TestConfiguration
    @Profile("test")
    static class ScrapperTestConfiguration {
        @Bean
        @Primary
        FileArchiver testArchiver() {
            Instant instant = Instant.parse("2007-12-03T10:15:30.00Z");
            return new FileArchiver(ARCHIVE_DIR, Clock.fixed(instant, ZoneId.of("America/Chicago")));
        }
    }

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private ImdbFileDownloader fileService;

    @Autowired
    private DatabaseUpdater testDatabaseUpdater;

    @BeforeAll
    static void setUpDirectories() throws IOException {
        Path TEMP_DIR = Files.createTempDirectory("temp");
        INPUT_DIR = Files.createDirectory(TEMP_DIR.resolve("input_files"));
        ARCHIVE_DIR = Files.createDirectory(TEMP_DIR.resolve("archive"));
    }

    @BeforeEach
    void loadFiles() throws IOException {
        File[] sampleFiles = SAMPLE_FILES_DIR.toFile().listFiles();
        Objects.requireNonNull(sampleFiles, SAMPLE_FILES_DIR + " not found");
        for (File sample : sampleFiles) {
            Files.copy(sample.toPath(), INPUT_DIR.resolve(sample.getName()));
        }
    }

    @AfterEach
    void cleanUp() throws IOException {
        // Wipe database
        JdbcTestUtils.deleteFromTables(jdbc, "imdb.episode", "imdb.show");

        // Clean directories of previous files
        cleanDirectory(INPUT_DIR);
        cleanDirectory(ARCHIVE_DIR);
    }

    @Test
    void testSampleFiles() throws ImdbFileParsingException {
        when(fileService.download(TITLES_FILE)).thenReturn(INPUT_DIR.resolve("title_sample.tsv"));
        when(fileService.download(RATINGS_FILE)).thenReturn(INPUT_DIR.resolve("ratings_sample.tsv"));
        when(fileService.download(EPISODES_FILE)).thenReturn(INPUT_DIR.resolve("episode_sample.tsv"));

        testDatabaseUpdater.updateDatabase();
        int epCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.episode");
        int showCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.show");
        assertEquals(3, showCount);
        assertEquals(135, epCount);
    }

    @Test
    void testBadFiles() {
        when(fileService.download(TITLES_FILE)).thenReturn(INPUT_DIR.resolve("title_sample.tsv"));
        when(fileService.download(RATINGS_FILE)).thenReturn(INPUT_DIR.resolve("ratings_sample.tsv"));
        when(fileService.download(EPISODES_FILE)).thenReturn(INPUT_DIR.resolve("bad_episode_sample.tsv"));

        assertThrows(ImdbFileParsingException.class, () -> testDatabaseUpdater.updateDatabase());

        File[] archivedFiles = ARCHIVE_DIR.toFile().listFiles();
        assertNotNull(archivedFiles);
        assertEquals(1, archivedFiles.length);
        assertEquals("bad_episode_sample_2007-12-03.tsv", archivedFiles[0].getName());
    }

    /**
     * Recursively delete all files but keep all directories intact
     */
    private static void cleanDirectory(Path root) throws IOException {
        File[] files = root.toFile().listFiles();
        Objects.requireNonNull(files, root + " not found");
        for(File f: files) {
            if(f.isDirectory()) {
                cleanDirectory(f.toPath());
            } else {
                Files.deleteIfExists(f.toPath());
            }
        }
    }
}
