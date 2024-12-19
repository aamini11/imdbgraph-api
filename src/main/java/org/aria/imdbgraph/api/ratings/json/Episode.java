package org.aria.imdbgraph.api.ratings.json;

/**
 * Data-class containing all the meta-data about a TV episode.
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
