package org.aria.imdbgraph;

import org.aria.imdbgraph.omdb.OmdbService;
import org.aria.imdbgraph.omdb.ShowRatings;
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

    @Autowired
    private OmdbService omdbService;

    @Value("${omdb.apikey}")
    private String apikey;

    @Test
    public void testAvatarSeasonSize() {
        ShowRatings avatarRatings = omdbService.getShowRating(AVATAR_ID);
        Assert.assertEquals(3, avatarRatings.getAllRatings().size());
    }
}
