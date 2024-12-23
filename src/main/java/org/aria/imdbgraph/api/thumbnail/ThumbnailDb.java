package org.aria.imdbgraph.api.thumbnail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        var thumbnailUrl = checkDatabase(showId);
        if (thumbnailUrl.isPresent()) {
            return thumbnailUrl;
        }

        // Image doesn't exist in database. Fetch new thumbnail from OMDB and save to db.
        var newThumbnailUrl = omdbClient.getThumbnailUrl(showId);
        if (newThumbnailUrl.isPresent()) {
            insertNewThumbnail(showId, newThumbnailUrl.get());
            return newThumbnailUrl;
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> checkDatabase(String showId) {
        List<String> results = jdbcTemplate.query(
                "SELECT thumbnail_url FROM imdb.thumbnails WHERE imdb_id = ?",
                (rs, rowNum) -> rs.getString(1),
                showId
        );
        if (results.size() != 1) {
            return Optional.empty();
        } else {
            return Optional.of(results.getFirst());
        }
    }

    private void insertNewThumbnail(String showId, String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO imdb.thumbnails(imdb_id, thumbnail_url) VALUES(?, ?)",
                showId, thumbnailUrl
        );
    }
}
