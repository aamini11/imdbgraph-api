package org.aria.imdbgraph;

import org.aria.imdbgraph.api.ratings.scraper.Scraper;
import org.aria.imdbgraph.api.ratings.scraper.Scraper.ImdbFileParsingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements ApplicationRunner {

    private final Scraper scraper;

    @Autowired
    public Main(Scraper scraper) {
        this.scraper = scraper;
    }

    @Override
    public void run(ApplicationArguments args) throws ImdbFileParsingException {
        if (args.getOptionNames().contains("run-scrapper")) {
            scraper.updateDatabase();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
