package org.aria.imdbgraph.ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class that supports search/query operations involving data from IMDB.
 */
@Service
class RatingsService {

    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    RatingsService(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Method that returns all ratings data for a specific show.
     *
     * @param showId The IMDB ID of the show to fetch ratings for.
     * @return A {@link Ratings} object containing all the show information
     * and episode ratings for a TV show. If in invalid ID is passed to the
     * method, an empty {@code Optional} is returned.
     */
    Optional<Ratings> getAllShowRatings(String showId) {
        Optional<Show> show = getShow(showId);
        if (show.isEmpty()) {
            return Optional.empty();
        }
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        String getEpisodesSQL = """
                SELECT episode_title,
                       season_num,
                       episode_num,
                       imdb_rating,
                       num_votes,
                       COALESCE(episode_title, 'No title was found') AS primary_title
                FROM imdb.episode
                WHERE show_id = :showId
                ORDER BY season_num, episode_num;
                """;
        List<Episode> allEpisodeRatings = jdbc.query(getEpisodesSQL, params, (rs, rowNum) -> {
            String title = rs.getString("episode_title");
            int season = rs.getInt("season_num");
            int episode = rs.getInt("episode_num");
            double imdbRating = rs.getDouble("imdb_rating");
            int numVotes = rs.getInt("num_votes");
            return new Episode(title, season, episode, imdbRating, numVotes);
        });
        return Optional.of(new Ratings(show.get(), allEpisodeRatings));
    }

    /**
     * Search method used by users to look for TV shows.
     *
     * @param searchQuery The search query provided by the user.
     * @return List of possible shows that match the search query.
     */
     List<Show> searchShows(String searchQuery) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchQuery);
        String sql = """
                SELECT imdb_id,
                       primary_title,
                       start_year,
                       end_year,
                       imdb_rating,
                       num_votes
                FROM imdb.show
                WHERE :searchTerm <% primary_title
                ORDER BY num_votes DESC
                LIMIT 10;
                """;
        return jdbc.query(sql, params, (rs, rowNum) -> mapToShow(rs));
    }

    private Optional<Show> getShow(String showId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        String sql = """
                SELECT
                  imdb_id,
                  primary_title,
                  start_year,
                  end_year,
                  imdb_rating,
                  num_votes
                FROM imdb.show
                WHERE imdb_id = :showId;
                """;
        try {
            Show show = jdbc.queryForObject(sql, params, (rs, rowNum) -> mapToShow(rs));
            return Optional.ofNullable(show);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static Show mapToShow(ResultSet rs) throws SQLException {
        String imdbId = rs.getString("imdb_id");
        String title = rs.getString("primary_title");
        String startYear = rs.getString("start_year");
        String endYear = rs.getString("end_year");
        double imdbRating = rs.getDouble("imdb_rating");
        int numVotes = rs.getInt("num_votes");

        return new Show(imdbId, title, startYear, endYear, imdbRating, numVotes);
    }
}
