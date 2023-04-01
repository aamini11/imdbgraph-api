package org.aria.imdbgraph.scrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * Utility class whose responsibility is to download and formats files provided
 * by IMDB. All files will be downloaded into a data directory specified by the
 * property 'imdbgraph.data.directory' in the spring application.properties
 * file.
 * <p>
 * Documentation for file formats is available here: <a href="https://www.imdb.com/interfaces/">link</a>
 */
final class ImdbFileDownloader {

    private static final Logger logger = LogManager.getLogger();
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    private final Path downloadDir;

    ImdbFileDownloader(Path downloadDir) {
        this.downloadDir = downloadDir;
    }

    /**
     * Enum to represent all the flat files provided by IMDB that need to be
     * parsed and downloaded into the database.
     */
    enum ImdbFile {
        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private final String fileName;

        ImdbFile(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Helper method to remove the .gz file extension from a file name.
         */
        String getUnzippedFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        /**
         * Fetches the URL where the file can be downloaded.
         */
        URL getDownloadUrl() {
            String downloadURL = UriComponentsBuilder
                    .fromUriString(BASE_DOWNLOAD_URL)
                    .pathSegment(fileName)
                    .toUriString();
            try {
                return new URL(downloadURL);
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Downloads all the files from IMDB which contain the data needed to be
     * parsed and loaded into the database.
     * <p>
     * NOTE: This method also unzips the file.
     *
     * @param fileToDownload The {@link ImdbFile} you want to download.
     * @return If the method was able to download the file successfully, it
     * returns its location.
     * @throws UncheckedIOException If the download method encountered any IO
     *                              errors and wasn't able to properly download
     *                              all the files, a file loading error will be
     *                              thrown.
     */
    Path download(ImdbFile fileToDownload) {
        URL downloadURL = fileToDownload.getDownloadUrl();
        File outputFile = downloadDir.resolve(fileToDownload.getUnzippedFileName()).toFile();
        // Unzip files as well.
        try (InputStream unzippedStream = new GZIPInputStream(downloadURL.openStream());
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
