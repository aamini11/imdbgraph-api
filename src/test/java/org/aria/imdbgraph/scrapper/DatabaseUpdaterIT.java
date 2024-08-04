package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.DatabaseUpdater.ImdbFileParsingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.aria.imdbgraph.scrapper.ImdbFileDownloader.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseUpdaterIT {

    private static final Path SAMPLE_FILES_DIR = Paths.get("src/test/resources/samples-files");
    private static Path INPUT_DIR;

    @MockBean
    private FileArchiver archiver;

    @MockBean
    private ImdbFileDownloader fileService;

    @Autowired
    private DatabaseUpdater databaseUpdater;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeAll
    static void setUp() throws IOException {
        Path TEMP_DIR = Files.createTempDirectory("temp");
        INPUT_DIR = Files.createDirectory(TEMP_DIR.resolve("input_files"));
    }

    @BeforeEach
    void loadFiles() throws IOException {
        // Wipe database
        JdbcTestUtils.deleteFromTables(jdbc, "imdb.episode", "imdb.thumbnails", "imdb.show");
        // Clean directories of previous files
        cleanDirectory(INPUT_DIR);

        File[] sampleFiles = SAMPLE_FILES_DIR.toFile().listFiles();
        Objects.requireNonNull(sampleFiles, SAMPLE_FILES_DIR + " not found");
        for (File sample : sampleFiles) {
            Files.copy(sample.toPath(), INPUT_DIR.resolve(sample.getName()));
        }
    }

    @Test
    void testSampleFiles() throws ImdbFileParsingException {
        when(fileService.download(TITLES_FILE)).thenReturn(INPUT_DIR.resolve("title_sample.tsv"));
        when(fileService.download(RATINGS_FILE)).thenReturn(INPUT_DIR.resolve("ratings_sample.tsv"));
        when(fileService.download(EPISODES_FILE)).thenReturn(INPUT_DIR.resolve("episode_sample.tsv"));

        databaseUpdater.updateDatabase();
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

        assertThrows(ImdbFileParsingException.class, () -> databaseUpdater.updateDatabase());
        verify(archiver, times(1)).archive(any());
    }

    /**
     * Recursively delete all files but keep all directories intact
     */
    private static void cleanDirectory(Path root) throws IOException {
        File[] files = root.toFile().listFiles();
        Objects.requireNonNull(files, root + " not found");
        for (File f : files) {
            if (f.isDirectory()) {
                cleanDirectory(f.toPath());
            } else {
                Files.deleteIfExists(f.toPath());
            }
        }
    }
}
