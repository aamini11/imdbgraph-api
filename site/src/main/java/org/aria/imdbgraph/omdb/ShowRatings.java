package main.java.org.aria.imdbgraph.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ShowRatings {

    private final List<SeasonRating> allRatings;

    ShowRatings(List<SeasonRating> allRatings) {
        this.allRatings = allRatings;
    }

    public String getEpisodeRating(int season, int episode) {
        SeasonRating seasonRating = allRatings.get(season-1);
        EpisodeRating episodeRating = seasonRating.getAllEpisodeRatings().get(episode-1);
        return episodeRating.getImdbRating();
    }

    public List<SeasonRating> getAllSeasonRatings() {
        return allRatings;
    }

    static final class SeasonRating {

        @JsonProperty("season")
        private final int seasonNumber;

        @JsonProperty("episodes")
        private final List<EpisodeRating> allEpisodeRatings;

        public SeasonRating(int seasonNumber, List<EpisodeRating> allEpisodeRatings) {
            this.seasonNumber = seasonNumber;
            this.allEpisodeRatings = allEpisodeRatings;
        }

        int getSeasonNumber() {
            return seasonNumber;
        }

        List<EpisodeRating> getAllEpisodeRatings() {
            return allEpisodeRatings;
        }
    }

    static final class EpisodeRating {

        @JsonProperty("title")
        private final String title;

        @JsonProperty("episode")
        private final int episode;

        @JsonProperty("imdbRating")
        private final String imdbRating;

        public EpisodeRating(String title, int episode, String imdbRating) {
            this.title = title;
            this.episode = episode;
            this.imdbRating = imdbRating;
        }

        public String getTitle() {
            return title;
        }

        public int getEpisode() {
            return episode;
        }

        String getImdbRating() {
            return imdbRating;
        }
    }
}
