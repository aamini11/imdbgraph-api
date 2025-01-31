package org.aria.imdbgraph.api.ratings;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS) // So @BeforeAll can be non-static.
class RatingsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate db;

    @BeforeAll
    void setUpData() {
        String data = """
                INSERT INTO imdb.show VALUES ('tt0417299', 'Avatar: The Last Airbender', '2005', '2008', 9.2, 193629);
                INSERT INTO imdb.show VALUES ('tt0944947', 'Game of Thrones', '2011', '2019', 9.4, 1563413);
                INSERT INTO imdb.show VALUES ('tt0096697', 'The Simpsons', '1989', null, 0, 0);
                
                INSERT INTO imdb.episode VALUES ('tt0417299', 'tt0772328', 'The Avatar Returns', 1, 2, 8.4, 1705);
                INSERT INTO imdb.episode VALUES ('tt0417299', 'tt5827942', 'Avatar: The Last Airbender', 1, 0, 0, 0);
                INSERT INTO imdb.episode VALUES ('tt0417299', 'tt0801470', 'The Boy in the Iceberg', 1, 1, 8.2, 1953);
                INSERT INTO imdb.episode VALUES ('tt0417299', 'tt0762600', 'The Avatar State', 2, 1, 0, 0);
                INSERT INTO imdb.episode VALUES ('tt0944947', 'tt1480055', 'Winter Is Coming', 1, 1, 9.1, 36939);
                INSERT INTO imdb.episode VALUES ('tt0944947', 'tt1668746', 'The Kingsroad', 1, 2, 8.8, 27976);
                INSERT INTO imdb.episode VALUES ('tt0944947', 'tt1829962', 'Lord Snow', 1, 3, 8.7, 26458);
                INSERT INTO imdb.episode VALUES ('tt0944947', 'tt1971833', 'The North Remembers', 2, 1, 8.9, 23735);
                INSERT INTO imdb.episode VALUES ('tt0944947', 'tt2069318', 'The Night Lands', 2, 2, 8.6, 22413);
                """;
        db.execute(data);
    }

    @AfterAll
    void wipeDb() {
        db.execute("DELETE FROM imdb.show");
    }

    @Test
    void testSearchingForAvatar() throws Exception {
        String expected = """
                [
                   {
                      "imdbId":"tt0417299",
                      "title":"Avatar: The Last Airbender",
                      "startYear":"2005",
                      "endYear":"2008",
                      "showRating":9.2,
                      "numVotes":193629
                   }
                ]
                """;
        mockMvc.perform(get("/search?q=Avatar"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    void testGettingAvatarRatings() throws Exception {
        String expected = """
                {
                   "show":{
                      "imdbId":"tt0417299",
                      "title":"Avatar: The Last Airbender",
                      "startYear":"2005",
                      "endYear":"2008",
                      "showRating":9.2,
                      "numVotes":193629
                   },
                   "allEpisodeRatings":{
                      "1":{
                         "0":{
                            "episodeTitle":"Avatar: The Last Airbender",
                            "season":1,
                            "episodeNumber":0,
                            "imdbRating":0.0,
                            "numVotes":0
                         },
                         "1":{
                            "episodeTitle":"The Boy in the Iceberg",
                            "season":1,
                            "episodeNumber":1,
                            "imdbRating":8.2,
                            "numVotes":1953
                         },
                         "2":{
                            "episodeTitle":"The Avatar Returns",
                            "season":1,
                            "episodeNumber":2,
                            "imdbRating":8.4,
                            "numVotes":1705
                         }
                      },
                      "2":{
                         "1":{
                            "episodeTitle":"The Avatar State",
                            "season":2,
                            "episodeNumber":1,
                            "imdbRating":0.0,
                            "numVotes":0
                         }
                      }
                   }
                }
                """;
        mockMvc.perform(get("/ratings/tt0417299"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    void testGettingRatingsWithInvalidId() throws Exception {
        mockMvc.perform(get("/ratings/123"))
                .andExpect(status().is4xxClientError());
    }
}
