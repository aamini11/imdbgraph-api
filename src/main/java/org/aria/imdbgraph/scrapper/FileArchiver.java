package org.aria.imdbgraph.scrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * Utility class used to archive IMDB files for auditing in-case of production
 * errors.
 */
final class FileArchiver {
    private static final Logger logger = LogManager.getLogger();

    // Limit for number of files allowed in archive directory
    private static final int DEFAULT_ARCHIVE_CAPACITY = 100;

    private final Path archiveDestination;
    private final int archiveCapacity;
    private final Clock clock;

    FileArchiver(Path archiveDestination, Clock clock) {
        this(archiveDestination, clock, DEFAULT_ARCHIVE_CAPACITY);
    }

    FileArchiver(Path archiveDestination, Clock clock, int archiveCapacity) {
        this.archiveDestination = archiveDestination;
        this.archiveCapacity = archiveCapacity;
        this.clock = clock;
    }

    /**
     * Takes all the files currently located in the download directory and
     * places a timestamped copy of each file in ./archive. The size limit for
     * the archive is specified by {@code archiveLimit}. If The limit is
     * exceeded the archive rolls over, which means the oldest files will be
     * deleted to make room for new files
     * <p>
     * This method is mainly used for auditing in case of production error.
     *
     * @param filesToArchive The files you want to archive.
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

    /**
     * Helper method to check if archive directory is full.
     */
    private boolean archiveIsFull() {
        File[] archives = archiveDestination.toFile().listFiles(File::isFile);
        Objects.requireNonNull(archives);
        return archives.length >= archiveCapacity;
    }

    /**
     * Helper method to find the oldest file in the archive directory.
     */
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
     *
     * @throws IllegalArgumentException File name must be non-empty
     */
    private String genArchiveFileName(String fileName) {
        // error check
        Objects.requireNonNull(fileName);
        if (fileName.isEmpty()) throw new IllegalArgumentException("Empty file name");

        int extensionPos = fileName.lastIndexOf('.'); // Index where file extension starts
        int fileNameStart = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (extensionPos <= fileNameStart) {
            extensionPos = fileName.length();
        }
        String timeStamp = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now(clock));
        return fileName.substring(0, extensionPos) + "_" + timeStamp + fileName.substring(extensionPos);
    }
}
