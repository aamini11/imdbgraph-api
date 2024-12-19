package org.aria.imdbgraph.api.ratings.json;

/**
 * Data-class containing all meta-data about a TV show.
 */
public record Show(
        String imdbId,
        String title,
        String startYear,
        String endYear,
        double showRating,
        int numVotes
) {
    public Show {
        if (numVotes < 0) {
            throw new IllegalArgumentException("Negative votes");
        }
        if (showRating < 0.0 || showRating > 10.0) {
            throw new IllegalArgumentException("Invalid rating");
        }
    }
}
