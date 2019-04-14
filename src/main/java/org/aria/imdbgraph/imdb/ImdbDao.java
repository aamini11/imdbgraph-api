package org.aria.imdbgraph.imdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Service class that supports basic IMDB operations like getting ratings for a show.
 */
@Repository
public class ImdbDao {

    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public ImdbDao(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns every ratings for a show along with some basic information about that show (title, year, etc...).
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return POJO containing the basic show info and ratings
     */
    public Ratings getAllShowRatings(String showId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);

        final String sql = "" +
                "WITH rankings AS (\n" +
                "    SELECT *\n" +
                "    FROM imdb.episode JOIN imdb.rating ON (episode_id = imdb_id)\n" +
                "    WHERE show_id = :showId \n" +
                "    ORDER BY season, episode.episode ASC)\n" +
                "SELECT " +
                "  COALESCE(primary_title, '(No title was found)') AS primary_title," +
                "  season," +
                "  episode," +
                "  imdb_rating, " +
                "  num_votes " +
                "FROM rankings JOIN imdb.episode_title USING (episode_id);";

        Show showInfo = getShow(showId);
        List<Episode> allEpisodeRatings = jdbc.query(sql, params, (rs, rowNum) -> {
            String title = rs.getString("primary_title");
            int season = rs.getInt("season");
            int episode = rs.getInt("episode");
            double imdbRating = rs.getDouble("imdb_rating");
            int numVotes = rs.getInt("num_votes");
            return new Episode(title, season, episode, imdbRating, numVotes);
        });
        return new Ratings(showInfo, allEpisodeRatings);
    }

    /**
     * Search for shows using the OMDB api
     *
     * @param searchTerm The search term to use
     * @return Returns a list of shows matching the search term provided
     */
    public List<Show> searchShows(String searchTerm) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchTerm);
        String sql = "" +
                "SELECT " +
                "  show_id," +
                "  primary_title," +
                "  start_year," +
                "  end_year," +
                "  COALESCE(imdb_rating, 0) as imdb_rating," +
                "  COALESCE(num_votes, 0) as num_votes\n" +
                "FROM imdb.show_title LEFT JOIN imdb.rating ON (show_id = imdb_id)\n" +
                "WHERE lower(primary_title) ~ lower(trim(:searchTerm))\n" +
                "ORDER BY num_votes DESC LIMIT 500;";
        return jdbc.query(sql, params, (rs, rowNum) -> showMapper(rs));
    }

    private Show getShow(String showId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        return jdbc.queryForObject("" +
                        "SELECT" +
                        "  show_id," +
                        "  primary_title, " +
                        "  start_year," +
                        "  end_year, " +
                        "  COALESCE(imdb_rating, 0) as imdb_rating, " +
                        "  COALESCE(num_votes, 0) as num_votes\n" +
                        "FROM imdb.show_title LEFT JOIN imdb.rating ON (show_id = imdb_id) " +
                        "WHERE show_id = :showId",
                params, (rs, rowNum) -> showMapper(rs));
    }

    private Show showMapper(ResultSet rs) throws SQLException {
        String imdbId = rs.getString("show_id");
        String title = rs.getString("primary_title");
        String startYear = rs.getString("start_year");
        String endYear = rs.getString("end_year");
        double imdbRating = rs.getDouble("imdb_rating");
        int numVotes = rs.getInt("num_votes");

        return new Show(imdbId, title, startYear, endYear, imdbRating, numVotes);
    }
}