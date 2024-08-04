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
}
