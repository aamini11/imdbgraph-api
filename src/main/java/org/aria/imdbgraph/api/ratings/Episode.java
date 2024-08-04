package org.aria.imdbgraph.api.ratings;

/**
 * Immutable data class containing all the meta-data and ratings information
 * about a specific TV episode.
 * <p>
 * Note: This class will also be serialized as a JSON object
 */
public record Episode(
        String episodeTitle,
        int season,
        int episodeNumber,
        double imdbRating,
        int numVotes
) {

    public Episode {
        if (numVotes < 0) {
            throw new IllegalArgumentException("Negative votes");
        }
        if (imdbRating < 0.0 || imdbRating > 10.0) {
            throw new IllegalArgumentException("Invalid rating");
        }
    }
}
