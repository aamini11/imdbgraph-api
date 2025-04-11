package org.aria.imdbgraph.api.ratings.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data-class containing all the IMDB episode ratings of a TV show.
 */
public record Ratings(
        Show show, Map<Integer,
        Map<Integer, Episode>> allEpisodeRatings
) {
    public Ratings(Show show, List<Episode> allEpisodeRatings) {
        this(show, toMap(allEpisodeRatings));
    }

    private static Map<Integer, Map<Integer, Episode>> toMap(List<Episode> episodeSequence) {
        Map<Integer, Map<Integer, Episode>> map = new LinkedHashMap<>();
        for (Episode e : episodeSequence) {
            Map<Integer, Episode> seasonRatings = map.computeIfAbsent(e.season(), _ -> new LinkedHashMap<>());
            seasonRatings.put(e.episodeNumber(), e);
        }
        return map;
    }
}
