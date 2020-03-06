package org.aria.imdbgraph.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import static org.aria.imdbgraph.scrapper.DatabaseUpdater.DailyUpdateError;

/**
 * Utility class whose responsibility is to download and formats files provided
 * by IMDB.
 * <p>
 * Documentation for file formats is available here:
 * https://www.imdb.com/interfaces/
 */
@Service
class ImdbFileService {

    private static final Logger logger = LoggerFactory.getLogger(ImdbFileService.class);
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    // Limit for number of files allowed in archive directory
    private static final int ARCHIVE_CAPACITY = 100;

    private final Path downloadDir;
    private final Path archiveDir;

    @Autowired
    ImdbFileService(@Value("${imdbgraph.data.directory}") String baseDir) {
        this.downloadDir = Paths.get("./data").toAbsolutePath().normalize();
        this.archiveDir = downloadDir.resolve("archive");
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
                throw new DailyUpdateError(e);
            }
        }
    }

    /**
     * Downloads all the files from IMDB which contain the data needed to be
     * parsed and loaded into the database.
     * <p>
     * NOTE: This method also unzips the file.
     *
     * @param filesToDownload The download method accepts a set of
     *                        {@code ImdbFile} which represents all the IMDB
     *                        files you want this method to download.
     * @return If the method was able to download the files successfully, it
     * returns a map where each {@code ImdbFile} points to the path where that
     * file was downloaded.
     *
     * @throws DailyUpdateError If the download method encountered any IO
     * errors and wasn't able to properly download all the files, a file loading
     * error will be thrown.
     */
    Map<ImdbFile, Path> download(Collection<ImdbFile> filesToDownload) {
        Map<ImdbFile, Path> downloadedFiles = new EnumMap<>(ImdbFile.class);
        for (ImdbFile f : filesToDownload) {
            URL downloadURL = f.getDownloadUrl();
            File outputFile = downloadDir.resolve(f.getUnzippedFileName()).toFile();
            // unzips files as well.
            try (InputStream unzippedStream = new GZIPInputStream(downloadURL.openStream());
                 ReadableByteChannel rbc = Channels.newChannel(unzippedStream);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                logger.info("Downloaded file: {} to {}", downloadURL, outputFile);

                downloadedFiles.put(f, outputFile.toPath());
            } catch (IOException e) {
                throw new DailyUpdateError(e);
            }
        }

        return downloadedFiles;
    }

    /**
     * Takes all the files currently located in the download directory and places
     * a timestamped copy of each file in ./archive. The archive folder
     * also has a limit of 100 files. If The limit is exceeded, the oldest files
     * will be deleted to make room.
     *
     * This method is used for auditing in case of production error.
     */
    void archive() {
        try {
            Files.createDirectories(archiveDir); //init archive directory

            File[] previousFiles = downloadDir.toFile().listFiles(File::isFile);
            if (previousFiles == null) throw new DailyUpdateError(downloadDir + " does not exist"); // error check
            for (File fileToArchive : previousFiles) {
                String timestampedFileName = genArchiveFileName(fileToArchive.getName());

                Path source = fileToArchive.toPath();
                Path dest = archiveDir.resolve(timestampedFileName);
                if (archiveIsFull()) {
                    Files.delete(findOldestArchivedFile());
                }
                Files.copy(source, dest);
            }
        } catch (IOException e) {
            throw new DailyUpdateError(e);
        }
    }

    /**
     * Helper method to check if archive directory is full.
     */
    private boolean archiveIsFull() {
        File[] archives = archiveDir.toFile().listFiles(File::isFile);
        if (archives == null) throw new DailyUpdateError("Archive dir missing");
        int numArchives = archives.length;
        return numArchives > ARCHIVE_CAPACITY;
    }

    /**
     * Helper method to find the oldest file in a directory.
     */
    private Path findOldestArchivedFile() throws IOException {
        File[] archives = archiveDir.toFile().listFiles(File::isFile);
        Map<Path, Instant> allFileDates = new HashMap<>();
        if (archives == null) throw new DailyUpdateError("");
        for (File archivedFile : archives) {
            BasicFileAttributes attr = Files.readAttributes(archivedFile.toPath(), BasicFileAttributes.class);
            Instant createDate = attr.creationTime().toInstant();
            allFileDates.put(archivedFile.toPath(), createDate);
        }

        Entry<Path, Instant> oldestEntry = Collections.min(allFileDates.entrySet(), (f1, f2) -> {
            Instant t1 = f1.getValue();
            Instant t2 = f2.getValue();
            return t1.compareTo(t2);
        });
        return oldestEntry.getKey();
    }

    /**
     * Utility method that takes a file name and tags it with a timestamp.
     * <p>
     * Example: file1.txt -> file1_2019-12-28.txt
     *
     * @throws IllegalArgumentException File name must be non-empty
     */
    private static String genArchiveFileName(String fileName) {
        // error check
        Objects.requireNonNull(fileName);
        if (fileName.isEmpty()) throw new IllegalArgumentException("Empty file name");

        int extensionPos = fileName.lastIndexOf('.'); // Index where file extension starts
        int fileNameStart = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (extensionPos <= fileNameStart) {
            extensionPos = fileName.length();
        }
        String timeStamp = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
        return fileName.substring(0, extensionPos) + "_" + timeStamp + fileName.substring(extensionPos);
    }
}
