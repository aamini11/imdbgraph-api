package org.aria.imdbgraph.api.ratings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempFile;

/**
 * Utility class used by {@link ImdbDataScraper} to download the latest data
 * files from IMDB.
 * <p>
 * <a href="https://www.imdb.com/interfaces/">More info about file format</a>
 */
@Service
public final class ImdbFileDownloader {

    private static final Logger log = LogManager.getLogger();

    private static final String DOWNLOAD_URL = "https://datasets.imdbws.com";

    /**
     * Downloads and unzips an IMDB data file and return the Path of the file if
     * succesful.
     */
    public Path download(ImdbFile file) {
        try (InputStream in = file.uri().toURL().openStream()) {
            Path out = createTempFile(file.name, ".tmp");
            copy(in, out);

            log.info("Downloaded file: {} to {}", file.uri(), out);
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public enum ImdbFile {
        TITLES("title.basics.tsv.gz"),
        EPISODES("title.episode.tsv.gz"),
        RATINGS("title.ratings.tsv.gz");

        private final String name;

        ImdbFile(String name) {
            this.name = name;
        }

        public URI uri() {
            return URI.create(DOWNLOAD_URL + "/" + name);
        }
    }
}
