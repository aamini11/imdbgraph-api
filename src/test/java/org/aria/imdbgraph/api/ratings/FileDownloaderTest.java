package org.aria.imdbgraph.api.ratings;

import org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.aria.imdbgraph.api.ratings.ImdbFileDownloader.ImdbFile.*;

class FileDownloaderTest {

    private final ImdbFileDownloader downloader = new ImdbFileDownloader();

    @Test
    void testDownloadingTitleFile() {
        testDownload(TITLES_FILE);
    }

    @Test
    void testDownloadingRatingsFile() {
        testDownload(RATINGS_FILE);
    }

    @Test
    void testDownloadingEpisodeFile() {
        testDownload(EPISODES_FILE);
    }

    private void testDownload(ImdbFile imdbFile) {
        long fileSize = downloader.download(imdbFile).toFile().length();
        Assertions.assertTrue(fileSize > 0);
    }
}
