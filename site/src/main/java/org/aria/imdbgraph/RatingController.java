package org.aria.imdbgraph;

import org.aria.imdbgraph.omdb.OmdbService;
import org.aria.imdbgraph.omdb.ShowRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RatingController {

    private final OmdbService ombdService;

    @Autowired
    public RatingController(OmdbService ombdService) {
        this.ombdService = ombdService;
    }

    @GetMapping(value = "/rating")
    public String getRating(@RequestParam(value="id") String showId, Model model) {
        model.addAttribute("ratingsData", ombdService.getShowRating(showId));
        return "rating_page.html";
    }
}
