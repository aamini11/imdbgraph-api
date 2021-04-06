package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile.*;

class FileDownloaderIT {

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
        ImdbFileService downloader = new ImdbFileService(temporaryFolder);
        long fileSize = downloader.download(imdbFile).toFile().length();
        Assertions.assertTrue(fileSize > 0);
    }
}
