package org.aria.imdbgraph.imdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static java.lang.Double.parseDouble;

/**
 * Data class containing all the ratings of a show and basic information about that show.
 */
public final class ShowRatings {

    private final ShowInfo showInfo;

    @JsonProperty
    private final Map<Integer, Map<Integer, Rating>> allRatings;

    static final class Rating {
        @JsonProperty
        final String episodeTitle;

        @JsonProperty
        final double imdbRating;

        Rating(String episodeTitle, String imdbRating) {
            this.episodeTitle = episodeTitle;

            if (imdbRating.equals("N/A")) {
                this.imdbRating = Double.NaN;
            } else {
                this.imdbRating = parseDouble(imdbRating);
            }
        }
    }

    ShowRatings(ShowInfo showInfo, Map<Integer, Map<Integer, Rating>> allRatings) {
        this.showInfo = showInfo;
        this.allRatings = allRatings;
    }

    public double getEpisodeRating(int season, int episode) {
        Map<Integer, Rating> seasonRating = allRatings.get(season);
        Rating episodeRating = seasonRating.get(episode);
        return episodeRating.imdbRating;
    }

    @JsonProperty
    public String getTitle() {
        return showInfo.title;
    }

    @JsonProperty
    public String getYear() {
        return showInfo.year;
    }

    @JsonProperty
    public String getImdbID() {
        return showInfo.imdbID;
    }

    @JsonProperty
    public int getTotalSeasons() {
        return showInfo.totalSeasons;
    }
}
