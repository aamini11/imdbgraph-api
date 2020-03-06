package org.aria.imdbgraph.scrapper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile;
import static org.aria.imdbgraph.scrapper.ImdbFileService.ImdbFile.*;

public class FileDownloaderUnitTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDownloadingTitleFile() throws IOException {
        testDownload(TITLES_FILE);
    }

    @Test
    public void testDownloadingRatingsFile() throws IOException {
        testDownload(RATINGS_FILE);
    }

    @Test
    public void testDownloadingEpisodeFile() throws IOException {
        testDownload(EPISODES_FILE);
    }

    @Test
    public void testArchivingTitleFile() throws IOException {
        File downloadDirectory = temporaryFolder.newFolder();
        ImdbFileService downloader = new ImdbFileService(downloadDirectory.toString());
        downloader.archive();
        downloader.download(Set.of(EPISODES_FILE));
        downloader.archive();
        downloader.download(Set.of(EPISODES_FILE));
    }

    private void testDownload(ImdbFile imdbFile) throws IOException {
        File downloadDirectory = temporaryFolder.newFolder();
        ImdbFileService downloader = new ImdbFileService(downloadDirectory.toString());
        Map<ImdbFile, Path> result = downloader.download(Set.of(imdbFile));
        Path downloadedFile = result.get(imdbFile);
        Assert.assertTrue(downloadedFile.toFile().length() > 0);
    }
}
