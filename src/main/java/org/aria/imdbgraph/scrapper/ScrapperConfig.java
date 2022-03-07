package org.aria.imdbgraph.scrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
class ScrapperConfig {

    private final String downloadDirPath;

    @Autowired
    public ScrapperConfig(@Value("${imdbgraph.data.directory}") String downloadDirPath) {
        this.downloadDirPath = downloadDirPath;
    }

    @Bean
    ImdbFileDownloader imdbFileDownloader() {
        Path downloadDir = Paths.get(downloadDirPath);
        return new ImdbFileDownloader(downloadDir);
    }

    @Bean
    FileArchiver archiver() {
        Path source = Paths.get(downloadDirPath);
        Clock texasClock = Clock.system(ZoneId.of("US/Central"));
        return new FileArchiver(source.resolve("archive"), texasClock);
    }
}
