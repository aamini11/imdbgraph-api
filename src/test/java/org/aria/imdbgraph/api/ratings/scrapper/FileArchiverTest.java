package org.aria.imdbgraph.api.ratings.scrapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

class FileArchiverTest {

    @TempDir
    Path tempDir;

    private Path archiveDir;

    @BeforeEach
    void setUp() {
        archiveDir = tempDir.resolve("archive");
    }

    private FileArchiver initFileArchiver(int archiveCapacity) {
        ZonedDateTime sampleTime = ZonedDateTime.parse("2007-12-03T10:15:30.00Z");
        Clock clock = Clock.fixed(sampleTime.toInstant(), ZoneId.of("America/Chicago"));
        return new FileArchiver(archiveDir, clock, archiveCapacity);
    }

    @Test
    void testArchivingSingleFile() throws IOException {
        FileArchiver fileArchiver = initFileArchiver(1);
        fileArchiver.archive(); // test no-op

        List<Path> files = List.of(
                Files.createFile(tempDir.resolve("file1")),
                Files.createFile(tempDir.resolve("file2")),
                Files.createFile(tempDir.resolve("file3"))
        );

        fileArchiver.archive(files.get(0));
        fileArchiver.archive(files.get(1));
        fileArchiver.archive(files.get(2));

        File[] archivedFiles = archiveDir.toFile().listFiles();
        Assertions.assertNotNull(archivedFiles);
        Assertions.assertEquals(1, archivedFiles.length);
        Assertions.assertEquals("file3_2007-12-03", archivedFiles[0].getName());
    }

    @Test
    void testArchivingMultipleFile() throws IOException {
        FileArchiver fileArchiver = initFileArchiver(4);
        fileArchiver.archive(); // test no-op

        fileArchiver.archive(
                Files.createFile(tempDir.resolve("file1")),
                Files.createFile(tempDir.resolve("file2"))
        );
        fileArchiver.archive(
                Files.createFile(tempDir.resolve("file3")),
                Files.createFile(tempDir.resolve("file4"))
        );
        fileArchiver.archive(
                Files.createFile(tempDir.resolve("file5")),
                Files.createFile(tempDir.resolve("file6"))
        );

        // verify
        File[] archivedFiles = getSortedFiles(archiveDir);
        Assertions.assertEquals(4, archivedFiles.length);
        Assertions.assertEquals("file3_2007-12-03", archivedFiles[0].getName());
        Assertions.assertEquals("file4_2007-12-03", archivedFiles[1].getName());
        Assertions.assertEquals("file5_2007-12-03", archivedFiles[2].getName());
        Assertions.assertEquals("file6_2007-12-03", archivedFiles[3].getName());
    }

    private static File[] getSortedFiles(Path archiveDir) {
        File[] archivedFiles = Objects.requireNonNull(archiveDir.toFile().listFiles());
        Arrays.sort(archivedFiles, Comparator.comparing(File::getName));
        return archivedFiles;
    }
}
