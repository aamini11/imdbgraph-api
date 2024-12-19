package org.aria.imdbgraph.api.ratings.scrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class used by {@link Scrapper} to archive any IMDB files that caused
 * parsing errors in production so that they can be audited later.
 */
@Service
final class FileArchiver {

    private static final Logger logger = LogManager.getLogger();

    // Limit for number of files allowed in archive directory
    private static final int DEFAULT_ARCHIVE_CAPACITY = 100;

    private final Path archiveDestination;
    private final int archiveCapacity;
    private final Clock clock;

    @Autowired
    FileArchiver(
            @Value("${imdbgraph.data.directory}") String downloadDirPath,
            Clock clock
    ) {
        this(Path.of(downloadDirPath), clock, DEFAULT_ARCHIVE_CAPACITY);
    }

    FileArchiver(Path archiveDestination, Clock clock, int archiveCapacity) {
        this.archiveDestination = archiveDestination;
        this.archiveCapacity = archiveCapacity;
        this.clock = clock;
    }

    /**
     * Takes all the files passed as an argument and places a timestamped copy
     * of each in ./archive. If The limit is exceeded, the archive rolls over by
     * deleting the oldest files.
     */
    void archive(Path... filesToArchive) {
        try {
            Files.createDirectories(archiveDestination); // nop if dir already exists

            for (Path fileToArchive : filesToArchive) {
                String timestampedName = genArchiveFileName(fileToArchive.getFileName().toString());
                if (archiveIsFull()) {
                    Path oldestFile = findOldestArchivedFile();
                    Files.delete(oldestFile);
                    logger.info("Archive full. Deleted {} from archive", oldestFile);
                }
                Path dest = archiveDestination.resolve(timestampedName);
                Files.move(fileToArchive, dest);
                logger.info("Archived {} to {}", fileToArchive, dest);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean archiveIsFull() {
        File[] archives = archiveDestination.toFile().listFiles(File::isFile);
        Objects.requireNonNull(archives);
        return archives.length >= archiveCapacity;
    }

    private Path findOldestArchivedFile() {
        File[] archives = archiveDestination.toFile().listFiles(File::isFile);
        Objects.requireNonNull(archives, archiveDestination + " does not exist");

        return Stream.of(archives)
                .min(Comparator.comparing(File::getName))
                .orElseThrow(() -> new AssertionError("Archive directory empty"))
                .toPath();
    }

    /**
     * Utility method that takes a file name and tags it with a timestamp.
     * <p>
     * Example: file1.txt -> file1_2019-12-28.txt
     */
    private String genArchiveFileName(String fileName) {
        // error check
        Objects.requireNonNull(fileName);
        if (fileName.isEmpty())
            throw new IllegalArgumentException("Empty file name");

        int extensionPos = fileName.lastIndexOf('.'); // Index where file extension starts
        int fileNameStart = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (extensionPos <= fileNameStart) {
            extensionPos = fileName.length();
        }
        String timeStamp = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now(clock));
        return fileName.substring(0, extensionPos) + "_" + timeStamp + fileName.substring(extensionPos);
    }
}
