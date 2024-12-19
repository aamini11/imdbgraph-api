package org.aria.imdbgraph;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ApplicationContext {

    @Bean
    public Clock texasClock() {
        return Clock.system(ZoneId.of("US/Central"));
    }
}
