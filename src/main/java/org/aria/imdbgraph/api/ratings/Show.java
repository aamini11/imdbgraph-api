package org.aria.imdbgraph.api.ratings;

/**
 * Immutable data class to represent all meta-data and ratings information about
 * a specific TV show.
 * <p>
 * Note: This class will also be serialized as a JSON object.
 */
public record Show(String imdbId, String title,
                   String startYear, String endYear,
                   double showRating, int numVotes) {

    public Show {
        if (numVotes < 0) {
            throw new IllegalArgumentException("Negative votes");
        }
        if (showRating < 0.0 || showRating > 10.0) {
            throw new IllegalArgumentException("Invalid rating");
        }
    }

    /**
     * A unique ID which IMDB assigns to every TV show, Movie, Episode, etc...
     */
    public String getImdbId() {
        return imdbId;
    }

    /**
     * The title of the TV show.
     */
    public String getTitle() {
        return title;
    }

    /**
     * The year the TV show started.
     */
    public String getStartYear() {
        return startYear;
    }

    /**
     * The year the TV show ended (Can be null if it hasn't ended yet)
     */
    public String getEndYear() {
        return endYear;
    }

    /**
     * The average rating of the TV show from a scale of (0.0 - 10.0)
     */
    public double getShowRating() {
        return showRating;
    }

    /**
     * The number of people that voted on the show's rating. Can be 0 if nobody
     * has voted on the TV show.
     */
    public int getNumVotes() {
        return numVotes;
    }
}
