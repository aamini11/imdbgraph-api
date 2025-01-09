package org.aria.imdbgraph.api.ratings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * Utility class used by {@link ImdbDataScraper} to download the latest data
 * files from IMDB.
 * <p>
 * <a href="https://www.imdb.com/interfaces/">More info about file format</a>
 */
@Service
public final class ImdbFileDownloader {

    private static final Logger logger = LogManager.getLogger();
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    public enum ImdbFile {
        TITLES("title.basics.tsv.gz"),
        EPISODES("title.episode.tsv.gz"),
        RATINGS("title.ratings.tsv.gz");

        private final String fileName;

        ImdbFile(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Remove .gz file extension from file name.
         */
        private String getUnzippedFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        private URL getDownloadUrl() {
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
        Path tempFile = createTempFile(fileToDownload.getUnzippedFileName());

        URL downloadURL = fileToDownload.getDownloadUrl();
        try (InputStream unzippedStream = new GZIPInputStream(downloadURL.openStream()); // Unzip
             ReadableByteChannel rbc = Channels.newChannel(unzippedStream);
             FileOutputStream fos = new FileOutputStream(tempFile.toFile())
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            logger.info("Downloaded file: {} to {}", downloadURL, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path createTempFile(String fileName) {
        try {
            return Files.createTempFile(fileName, ".tmp");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
