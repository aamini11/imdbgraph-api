package org.aria.imdbgraph.omdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.aria.imdbgraph.omdb.OmdbData.ShowInfo;

import java.util.List;

public class SearchResponse {

    @JsonProperty
    private final List<ShowInfo> search;

    @JsonProperty
    private final int totalResults;

    @JsonProperty
    private final boolean response;

    @JsonCreator
    SearchResponse(List<ShowInfo> searchResults, int totalResults, boolean response) {
        this.search = searchResults;
        this.totalResults = totalResults;
        this.response = response;
    }

    public List<ShowInfo> getSearch() {
        return search;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public boolean isResponse() {
        return response;
    }
}
