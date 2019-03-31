package org.aria.imdbgraph.imdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Static utility class that contains all the JSON objects required for the OMDB api.
 */
public class OmdbJSON {

    private OmdbJSON() {}

    /**
     * Information about a specific season of a show (including all the episode ratings for that season).
     */
    static final class SeasonInfo {

        @JsonProperty
        private final int season;

        @JsonProperty
        private final List<EpisodeInfo> episodes;

        SeasonInfo(int season, List<EpisodeInfo> episodes) {
            this.season = season;
            this.episodes = episodes;
        }

        int getSeason() {
            return season;
        }

        List<EpisodeInfo> getEpisodes() {
            return episodes;
        }
    }

    /**
     * Information about a specific episode of a show (including its rating)
     */
    static final class EpisodeInfo {

        @JsonProperty
        private final String title;

        @JsonProperty
        private final int episode;

        @JsonProperty
        private final String imdbRating;

        EpisodeInfo(String title, int episode, String imdbRating) {
            this.title = title;
            this.episode = episode;
            this.imdbRating = imdbRating;
        }

        String getTitle() {
            return title;
        }

        int getEpisode() {
            return episode;
        }

        String getImdbRating() {
            return imdbRating;
        }
    }

    /**
     * General information about a show (title, year, etc...)
     */
    static final class ShowInfo {

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

        ShowInfo(String title, String year, String imdbID, String poster, int totalSeasons) {
            this.title = title;
            this.year = year;
            this.imdbID = imdbID;
            this.poster = poster;
            this.totalSeasons = totalSeasons;
        }

        String getTitle() {
            return title;
        }

        String getYear() {
            return year;
        }

        String getImdbID() {
            return imdbID;
        }

        String getPoster() {
            return poster;
        }

        int getTotalSeasons() {
            return totalSeasons;
        }
    }

    public static final class SearchResponse {

        @JsonProperty
        private final List<ShowInfo> search;

        @JsonProperty
        private final int totalResults;

        @JsonProperty
        private final boolean response;

        @JsonCreator
        SearchResponse(List<ShowInfo> searchResults, int totalResults, boolean response) {
            this.search = searchResults;
            this.totalResults = totalResults;
            this.response = response;
        }

        public List<ShowInfo> getSearch() {
            return search;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public boolean isResponse() {
            return response;
        }
    }
}
