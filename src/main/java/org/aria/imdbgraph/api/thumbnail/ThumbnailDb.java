package org.aria.imdbgraph.api.thumbnail;

import org.aria.imdbgraph.modules.OmdbClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ThumbnailDb {

    private final JdbcTemplate jdbcTemplate;
    private final OmdbClient omdbClient;

    public ThumbnailDb(JdbcTemplate jdbcTemplate, OmdbClient omdbClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.omdbClient = omdbClient;
    }

    public Optional<String> getThumbnailUrl(String showId) {
        // Check cache
        List<String> results = jdbcTemplate.query(
                "SELECT thumbnail_url FROM imdb.thumbnails WHERE imdb_id = ?",
                (rs, _) -> rs.getString(1),
                showId
        );
        if (results.size() == 1) { // Return cached
            return Optional.of(results.getFirst());
        }

        // Check db + update cache.
        var url = omdbClient.getThumbnailUrl(showId); // Fetch
        if (url.isPresent()) { // Update cache
            jdbcTemplate.update(
                    "INSERT INTO imdb.thumbnails(imdb_id, thumbnail_url) VALUES(?, ?)",
                    showId, url
            );
            return url;
        } else {
            return Optional.empty();
        }
    }
}
