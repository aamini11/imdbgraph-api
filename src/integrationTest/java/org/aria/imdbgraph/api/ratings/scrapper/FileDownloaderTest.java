package org.aria.imdbgraph.api.ratings.scrapper;

import org.aria.imdbgraph.api.ratings.scrapper.ImdbFileDownloader.ImdbFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.aria.imdbgraph.api.ratings.scrapper.ImdbFileDownloader.ImdbFile.*;

class FileDownloaderTest {

    @TempDir
    Path temporaryFolder;

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
        ImdbFileDownloader downloader = new ImdbFileDownloader(temporaryFolder.toString());
        long fileSize = downloader.download(imdbFile).toFile().length();
        Assertions.assertTrue(fileSize > 0);
    }
}
