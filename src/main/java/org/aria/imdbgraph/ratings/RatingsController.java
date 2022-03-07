package org.aria.imdbgraph.ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
class RatingsController {

    private final RatingsService ratingsService;

    @Autowired
    RatingsController(RatingsService ratingsService) {
        this.ratingsService = ratingsService;
    }

    @GetMapping(value = "/ratings/{showId}")
    public Ratings getRatings(@PathVariable(value = "showId") String showId) {
        Optional<Ratings> ratings = ratingsService.getAllShowRatings(showId);
        if (ratings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found");
        }
        return ratings.get();
    }

    @GetMapping(value = "/search")
    public List<Show> search(@RequestParam(value="q") String searchTerm) {
        return ratingsService.searchShows(searchTerm);
    }
}
