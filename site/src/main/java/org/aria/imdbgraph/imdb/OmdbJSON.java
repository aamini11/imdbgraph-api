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

    private OmdbJSON() {}

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
    public static final class ShowInfoJSON {

        @JsonProperty
        private final String title;

        @JsonProperty
        private final String year;

        @JsonProperty
        private final String imdbID;

        @JsonProperty
        private final String poster;

        @JsonProperty
        private final int totalSeasons;

        @JsonProperty
        private final ImdbType type;

        @JsonProperty
        private final boolean response;

        @JsonProperty
        private final String error;

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

        public String getTitle() {
            return title;
        }

        public String getYear() {
            return year;
        }

        public String getImdbID() {
            return imdbID;
        }

        public String getPoster() {
            return poster;
        }

        public int getTotalSeasons() {
            return totalSeasons;
        }

        public ImdbType getType() {
            return type;
        }

        public boolean isResponse() {
            return response;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Response received from OMDB when making a search request.
     */
    public static final class SearchResponseJSON {

        private final List<ShowInfoJSON> search;
        private final int totalResults;
        private final boolean response;
        private final String error;

        SearchResponseJSON(List<ShowInfoJSON> search, int totalResults, boolean response, String error) {
            this.search = search;
            this.totalResults = totalResults;
            this.response = response;
            this.error = error;
        }

        public List<ShowInfoJSON> getSearch() {
            return search;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public boolean isResponse() {
            return response;
        }

        public String getError() {
            return error;
        }
    }
}
