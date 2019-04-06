package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbService;
import org.aria.imdbgraph.imdb.Ratings;
import org.aria.imdbgraph.imdb.Ratings.Episode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ImdbGraphApplication.class)
public class ImdbGraphApplicationTests {

    private static final String AVATAR_ID = "tt0417299";
    private static final String AMANDA_SHOW_ID = "tt0217910";

    private Ratings avatarRatings;

    @Autowired
    private ImdbService imdbService;

    @Before
    public void init() {
        avatarRatings = imdbService.getShowRating(AVATAR_ID);
    }

    @Test
    public void testAvatar() {
        Assert.assertEquals("Avatar: The Last Airbender", avatarRatings.getTitle());
        Assert.assertEquals("2003–2008", avatarRatings.getYear());
        Assert.assertEquals("tt0417299", avatarRatings.getImdbID());
        Assert.assertEquals(3, avatarRatings.getTotalSeasons());
    }

    @Test
    public void worstEpisode() {
        double worstRating = Double.POSITIVE_INFINITY;
        Episode worstEpisode = null;

        for (Map<Integer, Episode> seasonRatings : avatarRatings.getAllRatings().values()) {
            for (Episode episode : seasonRatings.values()) {
                double rating = episode.getImdbRating();
                if (rating < worstRating) {
                    worstRating = rating;
                    worstEpisode = episode;
                }
            }
        }

        assert worstEpisode != null;
        Assert.assertEquals("The Great Divide", worstEpisode.getEpisodeTitle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalType() {
        imdbService.getShowRating("tt0511020");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalID() {
        imdbService.getShowRating("asfgag");
    }

    @Test
    public void testSearch() {
//        SearchResultJ results = imdbService.omdbSearch("Simpsons");
//
//        boolean containsSimpsons = results.stream()
//                .anyMatch(show -> show.getTitle().equals("The Simpsons"));
//        Assert.assertTrue(containsSimpsons);
    }
}
