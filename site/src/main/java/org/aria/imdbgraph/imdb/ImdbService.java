package org.aria.imdbgraph.imdb;

import org.aria.imdbgraph.imdb.OmdbJSON.SearchResponseJSON;
import org.aria.imdbgraph.imdb.OmdbJSON.ShowInfoJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static org.aria.imdbgraph.imdb.ImdbService.ImdbType.series;
import static org.aria.imdbgraph.imdb.OmdbJSON.EpisodeRatingJSON;
import static org.aria.imdbgraph.imdb.OmdbJSON.SeasonRatingsJSON;
import static org.aria.imdbgraph.imdb.ShowRatings.Rating;

/**
 * Service class that supports basic IMDB operations like getting ratings for a show.
 */
@Service
public class ImdbService {

    private static final String BASE_URL = "https://www.omdbapi.com";

    // HTTP query params for OMDB api.
    private static final String API_KEY = "apikey";
    private static final String SHOW_ID = "i";
    private static final String SEARCH_TERM = "s";
    private static final String TYPE = "type";
    private static final String SEASON = "Season";

    enum ImdbType {
        series,
        movie,
        episode
    }

    private final String apiKey;
    private final RestTemplate restTemplate;

    @Autowired
    public ImdbService(@Value("${omdb.apikey}") String apiKey, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns every ratings for a show along with some basic information about that show (title, year, etc...).
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return POJO containing the basic show info and ratings
     */
    public ShowRatings getShowRating(String showId) {
        ShowInfo showInfo = getShowInfo(showId);

        // Get all ratings of a show season-by-season.
        Map<Integer, Map<Integer, Rating>> allSeasonsRatings = new LinkedHashMap<>();
        for (int seasonNum = 1; seasonNum <= showInfo.totalSeasons; seasonNum++) {
            String uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam(API_KEY, apiKey)
                    .queryParam(SHOW_ID, showId)
                    .queryParam(SEASON, seasonNum)
                    .toUriString();
            SeasonRatingsJSON response = restTemplate.getForObject(uri, SeasonRatingsJSON.class);

            // Transform omdb response into our own custom JSON.
            if (response != null) {
                Map<Integer, Rating> episodeRatings = new LinkedHashMap<>();
                for (EpisodeRatingJSON episodeInfo : response.episodes) {
                    // Increment episode num by 1. OMDB starts episodes from 0 for some reason.
                    episodeRatings.put(episodeInfo.episode, new Rating(episodeInfo.title, episodeInfo.imdbRating));
                }

                allSeasonsRatings.put(seasonNum, episodeRatings);
            }
        }

        return new ShowRatings(showInfo, allSeasonsRatings);
    }

    private ShowInfo getShowInfo(String showId) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam(API_KEY, apiKey)
                .queryParam(SHOW_ID, showId)
                .toUriString();

        ShowInfoJSON response = restTemplate.getForObject(uri, ShowInfoJSON.class);

        Objects.requireNonNull(response);
        if (!response.response) throw new IllegalArgumentException(response.error);
        if (response.type != series) throw new IllegalArgumentException("Not a series");

        return new ShowInfo(response);
    }

    /**
     * Method to search for IMDB shows.
     *
     * @param searchTerm The search term to use
     * @return Returns a map of all shows matching the search criteria. The keys in this case are the IMDB ids
     * of the shows and the entries are the shows themselves.
     */
    public List<ShowInfo> search(String searchTerm) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam(SEARCH_TERM, searchTerm)
                .queryParam(TYPE, series)
                .queryParam(API_KEY, apiKey)
                .toUriString();
        SearchResponseJSON response = restTemplate.getForObject(uri, SearchResponseJSON.class);
        Objects.requireNonNull(response);

        // transform from Omdb response JSON to our own custom JSON type.
        return response.search.stream()
                .map(json -> new ShowInfo(json))
                .collect(Collectors.toList());
    }
}
