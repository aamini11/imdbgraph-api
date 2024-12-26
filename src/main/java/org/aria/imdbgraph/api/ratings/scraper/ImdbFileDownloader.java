package org.aria.imdbgraph.api.ratings.scraper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * Utility class used by {@link Scraper} to download the latest data files from
 * IMDB.
 * <p>
 * <a href="https://www.imdb.com/interfaces/">More info about file format</a>
 */
@Service
public final class ImdbFileDownloader {

    private static final Logger logger = LogManager.getLogger();
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    private final Path downloadDir;

    public ImdbFileDownloader(@Value("${imdbgraph.data.directory}") String downloadDirPath) {
        this.downloadDir = Path.of(downloadDirPath);
    }

    public enum ImdbFile {
        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private final String fileName;

        ImdbFile(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Remove .gz file extension from file name.
         */
        String getUnzippedFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        URL getDownloadUrl() {
            URI downloadUri = UriComponentsBuilder
                    .fromUriString(BASE_DOWNLOAD_URL)
                    .pathSegment(fileName)
                    .build()
                    .toUri();
            try {
                return downloadUri.toURL();
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Downloads and unzips an IMDB data file and return the Path of the file if
     * succesful.
     */
    public Path download(ImdbFile fileToDownload) {
        URL downloadURL = fileToDownload.getDownloadUrl();
        File outputFile = downloadDir.resolve(fileToDownload.getUnzippedFileName()).toFile();
        if (!Files.isDirectory(downloadDir)) {
            throw new UncheckedIOException(new FileNotFoundException(downloadDir.toString()));
        }

        try (InputStream unzippedStream = new GZIPInputStream(downloadURL.openStream()); // Unzip
             ReadableByteChannel rbc = Channels.newChannel(unzippedStream);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            logger.info("Downloaded file: {} to {}", downloadURL, outputFile);
            return outputFile.toPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
