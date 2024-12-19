package org.aria.imdbgraph;

import org.aria.imdbgraph.api.ratings.scrapper.Scrapper;
import org.aria.imdbgraph.api.ratings.scrapper.Scrapper.ImdbFileParsingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
