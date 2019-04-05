package org.aria.imdbgraph.imdb;

import static org.aria.imdbgraph.imdb.OmdbJSON.*;

class ShowInfo {

    final String title;
    final String year;
    final String imdbID;
    final int totalSeasons;

    ShowInfo(ShowInfoJSON showInfo) {
        this.title = showInfo.title;
        this.year = showInfo.year;
        this.imdbID = showInfo.imdbID;
        this.totalSeasons = showInfo.totalSeasons;
    }
}
