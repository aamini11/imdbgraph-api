package org.aria.imdbgraph.scrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@Profile("production")
class ScrapperConfig {

    @Bean
    ImdbFileService fileService(@Value("${imdbgraph.data.directory}")
                                        String downloadDirPath) {
        Path downloadDir = Paths.get(downloadDirPath);
        return new ImdbFileService(downloadDir);
    }

    @Bean
    FileArchiver archiver(@Value("${imdbgraph.data.directory}")
                                  String downloadDirPath) {
        Path source = Paths.get(downloadDirPath);
        Clock texasClock = Clock.system(ZoneId.of("US/Central"));
        return new FileArchiver(source.resolve("archive"), texasClock);
    }
}
