package org.aria.imdbgraph.api.ratings;

import org.aria.imdbgraph.modules.ImdbFileDownloader;
import org.aria.imdbgraph.modules.ImdbFileDownloader.ImdbFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.aria.imdbgraph.modules.ImdbFileDownloader.ImdbFile.*;

class FileDownloaderTest {

    private final ImdbFileDownloader downloader = new ImdbFileDownloader();

    @Test
    void testDownloadingTitleFile() {
        testDownload(TITLES);
    }

    @Test
    void testDownloadingRatingsFile() {
        testDownload(RATINGS);
    }

    @Test
    void testDownloadingEpisodeFile() {
        testDownload(EPISODES);
    }

    private void testDownload(ImdbFile imdbFile) {
        long fileSize = downloader.download(imdbFile).toFile().length();
        Assertions.assertTrue(fileSize > 0);
    }
}
