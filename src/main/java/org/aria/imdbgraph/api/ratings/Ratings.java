package org.aria.imdbgraph.api.ratings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable data class containing all the ratings information about a specific
 * show, along with information about the TV show itself.
 * <p>
 * Note: This class will also be serialized as a JSON object and returned to the
 * front-end where it will be rendered as an actual graph.
 */
public final class Ratings {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allEpisodeRatings;

    Ratings(Show show, List<Episode> allEpisodeRatings) {
        this.show = show;
        this.allEpisodeRatings = toMap(allEpisodeRatings);
    }

    /**
     * Getter method which returns the show this {@code RatingsObject} object
     * is supposed to hold episode ratings for.
     *
     * @return A {@link Show} object containing all meta-data and ratings
     * information about the show.
     */
    @JsonProperty("show")
    public Show getShow() {
        return show;
    }

    /**
     * Getter method that returns all episode ratings data.
     *
     * @return 2D map where (season number, episode number) -&gt; (episode rating).
     */
    @JsonProperty("allEpisodeRatings")
    public Map<Integer, Map<Integer, Episode>> getAllEpisodeRatings() {
        return allEpisodeRatings;
    }

    private static Map<Integer, Map<Integer, Episode>> toMap(List<Episode> episodeSequence) {
        Map<Integer, Map<Integer, Episode>> map = new LinkedHashMap<>();
        for (Episode e : episodeSequence) {
            Map<Integer, Episode> seasonRatings = map.computeIfAbsent(e.getSeason(), key -> new LinkedHashMap<>());
            seasonRatings.put(e.getEpisodeNumber(), e);
        }
        return map;
    }
}
