package org.aria.imdbgraph.omdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 Class that supports basic OMDB operations such as getting the total number of seasons a TV series has.
 */
@Component
public class OmdbTemplate {

    private static final String BASE_URL = "https://www.omdbapi.com";

    private final String apiKey;
    private final RestTemplate restTemplate;

    public OmdbTemplate(@Value("${omdb.apikey}") String apiKey,
                        RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    private static class OmdbResponse {
        private int totalSeasons;

        int getTotalSeasons() {
            return totalSeasons;
        }

        void setTotalSeasons(int totalSeasons) {
            this.totalSeasons = totalSeasons;
        }
    }

    private int getNumSeasons(String showId) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .toUriString();
        OmdbResponse response = restTemplate.getForObject(uri, OmdbResponse.class);
        Objects.requireNonNull(response);
        return response.getTotalSeasons();
    }

    private Season getSeason(String showId, int season) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .queryParam("Season", season)
                .toUriString();
        return restTemplate.getForObject(uri, Season.class);
    }

    public List<Season> getAllSeasons(String showId) {
        int numSeasons = getNumSeasons(showId);
        List<Season> allSeasons = new ArrayList<>();
        for (int season = 1; season <= numSeasons; season++) {
            allSeasons.add(getSeason(showId, season));
        }
        return allSeasons;
    }
}
