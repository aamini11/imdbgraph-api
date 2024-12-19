package org.aria.imdbgraph.api.thumbnail;

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

    @Autowired
    public OmdbClient(@Value("${omdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Given an IMDB ID, return a thumbnail URL for that TV show. Return empty
     * result if given invalid ID.
     */
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

    record Rating(
            String source,
            String value
    ) {
    }

    record OmdbApiResponse(
            String title,
            String year,
            String rated,
            String released,
            String runtime,
            String genre,
            String director,
            String writer,
            String actors,
            String plot,
            String language,
            String country,
            String awards,
            String poster,
            List<Rating> ratings,
            String metascore,
            String imdbRating,
            String imdbVotes,
            String imdbID,
            String type,
            String totalSeasons,
            String response
    ) {
    }
}
