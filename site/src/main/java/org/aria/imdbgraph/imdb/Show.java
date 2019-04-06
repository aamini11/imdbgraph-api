package org.aria.imdbgraph.imdb;

import static org.aria.imdbgraph.imdb.OmdbJSON.*;

public class Show {

    private final String title;
    private final String year;
    private final String imdbID;
    private final int totalSeasons;

    Show(ShowInfoJSON showInfo) {
        this.title = showInfo.getTitle();
        this.year = showInfo.getYear();
        this.imdbID = showInfo.getImdbID();
        this.totalSeasons = showInfo.getTotalSeasons();
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }
}
