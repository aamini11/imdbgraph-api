package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbService;
import org.aria.imdbgraph.imdb.ShowRatings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ImdbGraphApplication.class)
public class ImdbGraphApplicationTests {

    private static final String AVATAR_ID = "tt0417299";
    private static final String AMANDA_SHOW_ID = "tt0217910";

    @Autowired
    private ImdbService imdbService;

    @Value("${omdb.apikey}")
    private String apikey;

    @Test
    public void testAvatarSeasonSize() {
        ShowRatings avatarRatings = imdbService.getShowRating(AVATAR_ID);
        Assert.assertEquals(3, avatarRatings.getTotalSeasons());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalType() {
        imdbService.getShowRating("tt0511020");
    }

    @Test
    public void testIllegalID() {
        imdbService.getShowRating("asfgag");
    }

    @Test
    public void test() {
        imdbService.getShowRating(AMANDA_SHOW_ID);
    }
}
