package org.aria.imdbgraph.api.ratings;

import org.aria.imdbgraph.modules.ImdbDataScraper;
import org.aria.imdbgraph.modules.ImdbDataScraper.ImdbFileParsingException;
import org.aria.imdbgraph.modules.ImdbFileDownloader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.copy;
import static org.aria.imdbgraph.modules.ImdbFileDownloader.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;
import static org.springframework.test.jdbc.JdbcTestUtils.deleteFromTables;

@SpringBootTest
@TestInstance(PER_CLASS) // So @AfterAll can be non-static.
class ImdbDataScraperTest {

    @Autowired
    private ImdbDataScraper scraper;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private ImdbFileDownloader fileDownloader;

    @TempDir
    Path inputDir;

    @BeforeEach
    void setUp() throws IOException {
        Path sampleFiles = Paths.get("src/test/resources/samples");
        copyDirectory(sampleFiles, inputDir);

        // Set up mocks.
        when(fileDownloader.download(TITLES)).thenReturn(inputDir.resolve("titles.tsv"));
        when(fileDownloader.download(RATINGS)).thenReturn(inputDir.resolve("ratings.tsv"));
        when(fileDownloader.download(EPISODES)).thenReturn(inputDir.resolve("episodes.tsv"));
    }

    @Test
    void testLoadingSampleFiles() {
        scraper.updateDatabase();
        assertEquals(3, countRowsInTable(jdbc, "imdb.show"));
        assertEquals(9, countRowsInTable(jdbc, "imdb.episode"));
    }

    @Test
    void testLoadingBadFiles() {
        // Override mock to point to bad file.
        when(fileDownloader.download(EPISODES)).thenReturn(inputDir.resolve("bad-episodes.tsv"));
        assertThrows(ImdbFileParsingException.class, () -> scraper.updateDatabase());
    }


    @AfterAll
    void wipeDb() {
        deleteFromTables(jdbc, "imdb.episode");
        deleteFromTables(jdbc, "imdb.show");
    }

    private static void copyDirectory(Path in, Path out) throws IOException {
        File[] sampleFiles = in.toFile().listFiles();
        if (sampleFiles == null) {
            throw new FileNotFoundException(in.toString());
        }
        for (File sample : sampleFiles) {
            copy(sample.toPath(), out.resolve(sample.getName()));
        }
    }
}
