package org.aria.imdbgraph;

import org.aria.imdbgraph.scrapper.DatabaseUpdater;
import org.aria.imdbgraph.scrapper.DatabaseUpdater.ImdbFileParsingException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements ApplicationRunner {

    private final DatabaseUpdater databaseUpdater;

    public Main(DatabaseUpdater databaseUpdater) {
        this.databaseUpdater = databaseUpdater;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws ImdbFileParsingException {
        if (args.getOptionNames().contains("run-scrapper")) {
            databaseUpdater.updateDatabase();
        }
    }
}
