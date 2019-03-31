package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.aria.imdbgraph.imdb.ImdbService.ShowRatings;
import static org.aria.imdbgraph.imdb.OmdbJSON.SearchResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ImdbGraphApplication.class)
public class ImdbGraphApplicationTests {

    private static final String AVATAR_ID = "tt0417299";

    @Autowired
    private ImdbService imdbService;

    @Value("${omdb.apikey}")
    private String apikey;

    @Test
    public void testAvatarSeasonSize() {
        ShowRatings avatarRatings = imdbService.getShowRating(AVATAR_ID);
        Assert.assertEquals(3, avatarRatings.getAllSeasons().size());
    }

    @Test
    public void testFailingSearch() {
        SearchResponse response = imdbService.search("a");
        Assert.assertFalse(response.isResponse());
    }

    @Test
    public void testSimpsonsSearch() {
        SearchResponse response = imdbService.search("Simpsons");
        Assert.assertTrue(response.isResponse());
    }
}
