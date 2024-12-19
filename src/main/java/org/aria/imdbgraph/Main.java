package org.aria.imdbgraph;

import org.aria.imdbgraph.api.ratings.scrapper.Scrapper;
import org.aria.imdbgraph.api.ratings.scrapper.Scrapper.ImdbFileParsingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class Main implements ApplicationRunner {

    private final Scrapper scrapper;

    @Autowired
    public Main(Scrapper scrapper) {
        this.scrapper = scrapper;
    }

    @Override
    public void run(ApplicationArguments args) throws ImdbFileParsingException {
        if (args.getOptionNames().contains("run-scrapper")) {
            scrapper.updateDatabase();
        }
    }

    @Scheduled(cron = "0 52 2 * * *")
    public void dailyDatabaseUpdate() throws ImdbFileParsingException {
        this.scrapper.updateDatabase();
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
