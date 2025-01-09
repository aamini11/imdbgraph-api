package org.aria.imdbgraph.api.ratings;

import org.aria.imdbgraph.api.ratings.ImdbDataScraper.ImdbFileParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;

@SpringBootTest
class RatingsTest {

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
        when(fileDownloader.download(TITLES_FILE)).thenReturn(inputDir.resolve("titles.tsv"));
        when(fileDownloader.download(RATINGS_FILE)).thenReturn(inputDir.resolve("ratings.tsv"));
        when(fileDownloader.download(EPISODES_FILE)).thenReturn(inputDir.resolve("episodes.tsv"));
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
        when(fileDownloader.download(EPISODES_FILE)).thenReturn(inputDir.resolve("bad-episodes.tsv"));
        assertThrows(ImdbFileParsingException.class, () -> scraper.updateDatabase());
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
