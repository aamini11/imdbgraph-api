package org.aria.imdbgraph.omdb;

import org.aria.imdbgraph.omdb.OmdbData.ShowInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.aria.imdbgraph.omdb.OmdbData.*;
import static org.aria.imdbgraph.omdb.OmdbData.SeasonInfo;

/**
 * Service class that supports basic OMDB api operations like getting ratings for a show
 */
@Service
public class OmdbService {

    /**
     * Data class containing all the ratings of a show.
     */
    public static final class ShowRatings {

        private final ShowInfo showInfo;
        private final List<SeasonInfo> allSeasons;

        ShowRatings(ShowInfo showInfo, List<SeasonInfo> allSeasons) {
            this.showInfo = showInfo;
            this.allSeasons = allSeasons;
        }

        public String getEpisodeRating(int season, int episode) {
            SeasonInfo seasonRating = allSeasons.get(season - 1);
            EpisodeInfo episodeRating = seasonRating.getEpisodes().get(episode - 1);
            return episodeRating.getImdbRating();
        }

        public ShowInfo getShowInfo() {
            return showInfo;
        }

        public List<SeasonInfo> getAllSeasons() {
            return allSeasons;
        }
    }

    private static final String BASE_URL = "https://www.omdbapi.com";

    private final String apiKey;
    private final RestTemplate restTemplate;

    @Autowired
    public OmdbService(@Value("${omdb.apikey}") String apiKey, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    public ShowRatings getShowRating(String showId) {
        ShowInfo showInfo = getShowInfo(showId);
        List<SeasonInfo> allSeasons = new ArrayList<>();
        for (int season = 1; season <= showInfo.getTotalSeasons(); season++) {
            allSeasons.add(getSeason(showId, season));
        }
        return new ShowRatings(showInfo, allSeasons);
    }

    public SearchResponse search(String searchTerm) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("s", searchTerm)
                .queryParam("type", "series")
                .queryParam("apikey", apiKey)
                .toUriString();
        return restTemplate.getForObject(uri, SearchResponse.class);
    }

    private ShowInfo getShowInfo(String showId) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .toUriString();
        ShowInfo response = restTemplate.getForObject(uri, ShowInfo.class);
        Objects.requireNonNull(response);
        return response;
    }

    private SeasonInfo getSeason(String showId, int season) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .queryParam("Season", season)
                .toUriString();
        return restTemplate.getForObject(uri, SeasonInfo.class);
    }
}
