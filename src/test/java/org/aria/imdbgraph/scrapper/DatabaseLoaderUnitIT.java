package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.DatabaseUpdater.ImdbFileParsingError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseLoaderUnitIT {

    private static final Path SAMPLE_FILES_DIR = Paths.get("src/test/resources/samples-files");

    @TempDir
    Path workspace;

    private Path input;
    private Path archive;

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private ImdbFileService fileService;

    private DatabaseUpdater databaseUpdater;

    @BeforeEach
    void setUp() throws IOException {
        setUpDirectories();
        loadSampleFiles();
        cleanDb();

        Instant instant = Instant.parse("2007-12-03T10:15:30.00Z");
        FileArchiver archiver = new FileArchiver(archive, Clock.fixed(instant, ZoneId.of("America/Chicago")));
        databaseUpdater = new DatabaseUpdater(jdbc, fileService, archiver);
    }

    @Test
    void testSampleFiles() throws ImdbFileParsingError {
        when(fileService.download(TITLES_FILE)).thenReturn(input.resolve("title_sample.tsv"));
        when(fileService.download(RATINGS_FILE)).thenReturn(input.resolve("ratings_sample.tsv"));
        when(fileService.download(EPISODES_FILE)).thenReturn(input.resolve("episode_sample.tsv"));

        databaseUpdater.updateDatabase();
        int epCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.episode");
        int showCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.show");
        assertEquals(3, showCount);
        assertEquals(135, epCount);
    }

    @Test
    void testBadFiles() {
        when(fileService.download(TITLES_FILE)).thenReturn(input.resolve("title_sample.tsv"));
        when(fileService.download(RATINGS_FILE)).thenReturn(input.resolve("ratings_sample.tsv"));
        when(fileService.download(EPISODES_FILE)).thenReturn(input.resolve("bad_episode_sample.tsv"));

        assertThrows(ImdbFileParsingError.class, () -> databaseUpdater.updateDatabase());

        File[] archivedFiles = archive.toFile().listFiles();
        assertNotNull(archivedFiles);
        assertEquals(1, archivedFiles.length);
        assertEquals("title_sample_2007-12-03.tsv", archivedFiles[0].getName());
    }

    private void setUpDirectories() throws IOException {
        input = Files.createDirectory(workspace.resolve("input"));
        archive = Files.createDirectory(workspace.resolve("archive"));
    }

    private void loadSampleFiles() throws IOException {
        File[] sampleFiles = SAMPLE_FILES_DIR.toFile().listFiles();
        Objects.requireNonNull(sampleFiles, SAMPLE_FILES_DIR + " not found");
        for (File sample : sampleFiles) {
            Files.copy(sample.toPath(), input.resolve(sample.getName()));
        }
    }

    private void cleanDb() {
        JdbcTestUtils.deleteFromTables(jdbc, "imdb.episode", "imdb.show");
    }
}
