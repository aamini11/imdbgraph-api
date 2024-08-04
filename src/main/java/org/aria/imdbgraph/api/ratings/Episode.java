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

    /**
     * Title of the episode
     */
    public String getEpisodeTitle() {
        return episodeTitle;
    }

    /**
     * Season number of episode
     */
    public int getSeason() {
        return season;
    }

    /**
     * Returns episode number
     */
    public int getEpisodeNumber() {
        return episodeNumber;
    }

    /**
     * The average IMDB rating of the episode (0.0 - 10.0)
     */
    public double getImdbRating() {
        return imdbRating;
    }

    /**
     * Total number of votes for the episode (&gt; 0)
     */
    public int getNumVotes() {
        return numVotes;
    }
}
