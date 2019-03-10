package org.aria.imdbgraph.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ShowRatings {

    private final List<SeasonInfo> allSeasons;

    ShowRatings(@JsonProperty("allSeasons") List<SeasonInfo> allSeasons) {
        this.allSeasons = allSeasons;
    }

    public List<SeasonInfo> getAllSeaons() {
        return allSeasons;
    }

    public String getEpisodeRating(int season, int episode) {
        SeasonInfo seasonInfo = getAllSeaons().get(season-1);
        EpisodeInfo epsiodeInfo = seasonInfo.getAllEpisodes().get(episode-1);
        return epsiodeInfo.getImdbRating();
    }

    public static final class SeasonInfo {
        private final int season;
        private final List<EpisodeInfo> allEpisodes;

        public SeasonInfo(@JsonProperty("season") int season, @JsonProperty("episodes") List<EpisodeInfo> allEpisodes) {
            this.season = season;
            this.allEpisodes = allEpisodes;
        }

        public int getSeason() {
            return season;
        }

        List<EpisodeInfo> getAllEpisodes() {
            return allEpisodes;
        }
    }

    private final static class EpisodeInfo {
        private final String title;
        private final int episode;
        private final String imdbRating;

        public EpisodeInfo(@JsonProperty("title") String title,
                           @JsonProperty("episode") int episode,
                           @JsonProperty("imdbRating") String imdbRating) {
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
