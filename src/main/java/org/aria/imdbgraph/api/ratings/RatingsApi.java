package org.aria.imdbgraph.api.ratings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aria.imdbgraph.api.ratings.json.Ratings;
import org.aria.imdbgraph.api.ratings.json.Show;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class RatingsApi {

    private static final Logger logger = LogManager.getLogger(RatingsApi.class);

    private final RatingsDb ratingsDb;

    public RatingsApi(RatingsDb ratingsDb) {
        this.ratingsDb = ratingsDb;
    }

    @GetMapping(value = "/ratings/{showId}")
    public Ratings getRatings(@PathVariable(value = "showId") String showId) {
        Optional<Ratings> ratings = ratingsDb.getAllShowRatings(showId);
        if (ratings.isEmpty()) {
            logger.info("Show not found: {}", showId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found");
        }
        logger.info("Returning ratings for show: {}", showId);
        return ratings.get();
    }

    @GetMapping(value = "/search")
    public List<Show> search(@RequestParam(value = "q") String searchTerm) {
        logger.info("Searching for q=\"{}\"", searchTerm);
        return ratingsDb.searchShows(searchTerm);
    }
}
