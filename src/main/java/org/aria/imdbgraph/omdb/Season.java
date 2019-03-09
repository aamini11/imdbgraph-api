package org.aria.imdbgraph.omdb;

import java.util.List;

public class Season {

    private static class EpisodeInfo {
        private String title;
        private int episode;
        private String imdbRating;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getEpisode() {
            return episode;
        }

        public void setEpisode(int episode) {
            this.episode = episode;
        }

        public String getImdbRating() {
            return imdbRating;
        }

        public void setImdbRating(String imdbRating) {
            this.imdbRating = imdbRating;
        }
    }

    private int season;
    private List<EpisodeInfo> episodes;

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public List<EpisodeInfo> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<EpisodeInfo> episodes) {
        this.episodes = episodes;
    }
}
