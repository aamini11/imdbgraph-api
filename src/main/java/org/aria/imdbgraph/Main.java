package org.aria.imdbgraph;

import org.aria.imdbgraph.modules.ImdbDataScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements ApplicationRunner {

    private final ImdbDataScraper scraper;

    @Autowired
    public Main(ImdbDataScraper scraper) {
        this.scraper = scraper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.getOptionNames().contains("run-scraper")) {
            scraper.updateDatabase();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
