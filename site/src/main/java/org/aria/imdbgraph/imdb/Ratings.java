package org.aria.imdbgraph.imdb;

import java.util.Map;

import static java.lang.Double.parseDouble;

/**
 * Data class containing all the ratings of a show and basic information about that show.
 */
public final class Ratings {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allRatings;

    public static final class Episode {
        private final String episodeTitle;
        private final double imdbRating;

        Episode(String episodeTitle, String imdbRating) {
            this.episodeTitle = episodeTitle;

            if (imdbRating.equals("N/A")) {
                this.imdbRating = Double.NaN;
            } else {
                this.imdbRating = parseDouble(imdbRating);
            }
        }

        public String getEpisodeTitle() {
            return episodeTitle;
        }

        public double getImdbRating() {
            return imdbRating;
        }
    }

    Ratings(Show show, Map<Integer, Map<Integer, Episode>> allRatings) {
        this.show = show;
        this.allRatings = allRatings;
    }

    public Map<Integer, Map<Integer, Episode>> getAllRatings() {
        return allRatings;
    }

    public String getTitle() {
        return show.getTitle();
    }

    public String getYear() {
        return show.getYear();
    }

    public String getImdbID() {
        return show.getImdbID();
    }

    public int getTotalSeasons() {
        return show.getTotalSeasons();
    }

    public double getEpisodeRating(int season, int episode) {
        Map<Integer, Episode> seasonRating = allRatings.get(season);
        Episode episodeRating = seasonRating.get(episode);
        return episodeRating.imdbRating;
    }

}
