package org.aria.imdbgraph.imdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static org.aria.imdbgraph.imdb.ImdbService.ImdbType;

/**
 * Static utility class that contains all the JSON objects used by the OMDB api. Any response from the OMDB api
 * will map to one of these JSONs.
 * <p>
 * Note: These classes are only used for reading responses from the OMDB api. They are never sent to the frontend as
 * responses and no frontend code relies on them. Instead, all these classes are transformed to our own
 * custom JSON responses. This is because I plan on migrating away from OMDB in the future and want to make sure
 * no existing code will break when I do so.
 */
class OmdbJSON {

    private OmdbJSON() {
    }

    /**
     * Information about a specific season of a show (including all the episode ratings for that season).
     */
    static final class SeasonRatingsJSON {

        @JsonProperty
        final int season;

        @JsonProperty
        final List<EpisodeRatingJSON> episodes;

        SeasonRatingsJSON(int season, List<EpisodeRatingJSON> episodes) {
            this.season = season;
            this.episodes = episodes;
        }
    }

    /**
     * Information about a specific episode of a show (including its rating)
     */
    static final class EpisodeRatingJSON {

        @JsonProperty
        final String title;

        @JsonProperty
        final int episode;

        @JsonProperty
        final String imdbRating;

        EpisodeRatingJSON(String title, int episode, String imdbRating) {
            this.title = title;
            this.episode = episode;
            this.imdbRating = imdbRating;
        }
    }

    /**
     * General information about a show (title, year, etc...)
     */
    static final class ShowInfoJSON {

        @JsonProperty
        final String title;

        @JsonProperty
        final String year;

        @JsonProperty
        final String imdbID;

        @JsonProperty
        final String poster;

        @JsonProperty
        final int totalSeasons;

        @JsonProperty
        final ImdbType type;

        @JsonProperty
        final boolean response;

        @JsonProperty
        final String error;

        public ShowInfoJSON(String title, String year, String imdbID, String poster, int totalSeasons, ImdbType type, boolean response, String error) {
            this.title = title;
            this.year = year;
            this.imdbID = imdbID;
            this.poster = poster;
            this.totalSeasons = totalSeasons;
            this.type = type;
            this.response = response;
            this.error = error;
        }
    }

    /**
     * Response received from OMDB when making a search request.
     */
    static final class SearchResponseJSON {

        @JsonProperty
        final List<ShowInfoJSON> search;

        @JsonProperty
        final int totalResults;

        @JsonProperty
        final boolean response;

        SearchResponseJSON(List<ShowInfoJSON> searchResults, int totalResults, boolean response) {
            this.search = searchResults;
            this.totalResults = totalResults;
            this.response = response;
        }
    }
}
