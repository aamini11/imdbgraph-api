package org.aria.imdbgraph.api.thumbnail;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for fetching thumbnail URLs from the open IMDB API
 * (OMDB).
 */
@Service
public class OmdbClient {

    private static final Logger logger = LogManager.getLogger();

    private static final int LIMIT = 1000;

    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final AtomicInteger counter = new AtomicInteger(1);

    private record OmdbApiResponse(
            @JsonProperty("Title") String title,
            @JsonProperty("Year") String year,
            @JsonProperty("Rated") String rated,
            @JsonProperty("Released") String released,
            @JsonProperty("Runtime") String runtime,
            @JsonProperty("Genre") String genre,
            @JsonProperty("Director") String director,
            @JsonProperty("Writer") String writer,
            @JsonProperty("Actors") String actors,
            @JsonProperty("Plot") String plot,
            @JsonProperty("Language") String language,
            @JsonProperty("Country") String country,
            @JsonProperty("Awards") String awards,
            @JsonProperty("Poster") String poster,
            @JsonProperty("Ratings") List<Rating> ratings,
            @JsonProperty("Metascore") String metascore,
            @JsonProperty("imdbRating") String imdbRating,
            @JsonProperty("imdbVotes") String imdbVotes,
            @JsonProperty("imdbID") String imdbID,
            @JsonProperty("Type") String type,
            @JsonProperty("totalSeasons") String totalSeasons,
            @JsonProperty("Response") String response
    ) {
    }

    private record Rating(
            @JsonProperty("Source") String source,
            @JsonProperty("Value") String value
    ) {
    }

    @Autowired
    public OmdbClient(@Value("${omdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public Optional<String> getThumbnailUrl(String imdbId) {
        int curr = counter.getAndIncrement();
        if (curr > LIMIT) {
            logger.info("OMDB API Limit reached");
            return Optional.empty();
        }

        OmdbApiResponse response = restClient.get()
                .uri("https://www.omdbapi.com/?i={i}&apikey={apikey}", imdbId, apiKey)
                .retrieve()
                .body(OmdbApiResponse.class);
        if (response == null || response.poster() == null) {
            return Optional.empty();
        } else {
            var poster = response.poster();
            logger.info("Fetching OMDB thumbnail ({} out of {}): {}", curr, LIMIT, poster);
            return Optional.of(poster);
        }
    }
}
