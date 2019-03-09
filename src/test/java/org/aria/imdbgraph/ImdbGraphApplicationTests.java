package org.aria.imdbgraph;

import org.aria.imdbgraph.omdb.OmdbTemplate;
import org.aria.imdbgraph.omdb.Season;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ImdbGraphApplication.class)
public class ImdbGraphApplicationTests {

    @Autowired
    private OmdbTemplate omdbTemplate;

    @Value("${omdb.apikey}")
    private String apikey;

    @Test
    public void testSeasonGet() {
        List<Season> response = omdbTemplate.getAllSeasons("tt0096697");
        Assert.assertEquals(30, response.size());
    }
}
