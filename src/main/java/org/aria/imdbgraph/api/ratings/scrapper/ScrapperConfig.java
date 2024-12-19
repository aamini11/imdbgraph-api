package org.aria.imdbgraph.api.ratings.scrapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class ScrapperConfig {

    @Bean
    Clock texasClock() {
        return Clock.system(ZoneId.of("US/Central"));
    }
}
