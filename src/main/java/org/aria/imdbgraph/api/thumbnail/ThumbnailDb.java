package org.aria.imdbgraph.api.thumbnail;

import org.aria.imdbgraph.modules.OmdbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ThumbnailDb {

    private final JdbcTemplate jdbcTemplate;
    private final OmdbClient omdbClient;

    @Autowired
    public ThumbnailDb(JdbcTemplate jdbcTemplate, OmdbClient omdbClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.omdbClient = omdbClient;
    }

    public Optional<String> getThumbnailUrl(String showId) {
        // Check cache
        String cachedUrl = jdbcTemplate.queryForObject(
                "SELECT thumbnail_url FROM imdb.thumbnails WHERE imdb_id = ?",
                (rs, _) -> rs.getString(1),
                showId
        );
        if (cachedUrl != null) {
            return Optional.of(cachedUrl);
        }

        // If image doesn't exist in database, fetch new thumbnail from OMDB and
        // save to db.
        var url = omdbClient.getThumbnailUrl(showId); // Fetch
        if (url.isPresent()) {
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
