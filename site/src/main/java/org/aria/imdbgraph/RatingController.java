package main.java.org.aria.imdbgraph;

import main.java.org.aria.imdbgraph.omdb.OmdbService;
import main.java.org.aria.imdbgraph.omdb.ShowRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RatingController {

    private final OmdbService ombdService;

    @Autowired
    public RatingController(OmdbService ombdService) {
        this.ombdService = ombdService;
    }

    @GetMapping(value = "/rating", produces = "application/json")
    public ShowRatings getRating(@RequestParam(value="id") String showId) {
        return ombdService.getShowRating(showId);
    }
}
