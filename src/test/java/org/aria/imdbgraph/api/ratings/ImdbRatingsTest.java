package org.aria.imdbgraph.api.ratings;

import org.aria.imdbgraph.api.ratings.ImdbDataScraper.ImdbFileParsingException;
import org.aria.imdbgraph.api.ratings.json.Ratings;
import org.aria.imdbgraph.api.ratings.json.Show;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.copy;
import static org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ImdbRatingsTest {

    private static final Path TEST_FILES_FOLDER = Paths.get("src/test/resources/samples-files");

    @TempDir
    Path workingDir;

    @BeforeEach
    void setUp() throws IOException {
        loadTestFiles(workingDir);

        // Set up mocks.
        when(fileDownloader.download(TITLES_FILE)).thenReturn(workingDir.resolve("title_sample.tsv"));
        when(fileDownloader.download(RATINGS_FILE)).thenReturn(workingDir.resolve("ratings_sample.tsv"));
        when(fileDownloader.download(EPISODES_FILE)).thenReturn(workingDir.resolve("episode_sample.tsv"));
    }

    @Autowired
    private ImdbDataScraper scraper;

    @Autowired
    private RatingsDb db;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private ImdbFileDownloader fileDownloader;

    // First two tests must run before subsequent tests.
    @Test
    @Order(1)
    void testLoadingSampleFiles() {
        scraper.updateDatabase();
        assertEquals(3, countRowsInTable(jdbc, "imdb.show"));
        assertEquals(135, countRowsInTable(jdbc, "imdb.episode"));
    }

    @Test
    @Order(2)
    void testLoadingBadFiles() {
        // Override mock to point to bad file.
        when(fileDownloader.download(EPISODES_FILE)).thenReturn(workingDir.resolve("bad_episode_sample.tsv"));
        assertThrows(ImdbFileParsingException.class, () -> scraper.updateDatabase());
    }

    @Test
    void testSearchingForAvatar() {
        List<Show> shows = db.searchShows("Ava");
        assertEquals(1, shows.size());
        assertEquals("Avatar: The Last Airbender", shows.getFirst().title());
    }

    @Test
    void testGettingAvatarRatings() {
        Optional<Ratings> ratingsMaybe = db.getAllShowRatings("tt0417299");
        assertTrue(ratingsMaybe.isPresent());

        Ratings ratings = ratingsMaybe.get();
        assertEquals("Avatar: The Last Airbender", ratings.show().title());
        assertEquals(3, ratings.allEpisodeRatings().size());

        assertEquals(21, ratings.allEpisodeRatings().get(1).size());
        assertEquals(20, ratings.allEpisodeRatings().get(2).size());
        assertEquals(21, ratings.allEpisodeRatings().get(3).size());
    }

    @Test
    void testGettingRatingsWithInvalidId() {
        Optional<Ratings> ratingsMaybe = db.getAllShowRatings("");
        assertTrue(ratingsMaybe.isEmpty());
    }

    private static void loadTestFiles(Path dir) throws IOException {
        File[] sampleFiles = TEST_FILES_FOLDER.toFile().listFiles();
        if (sampleFiles == null) {
            throw new FileNotFoundException(TEST_FILES_FOLDER.toString());
        }
        for (File sample : sampleFiles) {
            copy(sample.toPath(), dir.resolve(sample.getName()));
        }
    }
}
