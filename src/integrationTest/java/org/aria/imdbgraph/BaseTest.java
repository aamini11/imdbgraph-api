package org.aria.imdbgraph;

import org.aria.imdbgraph.api.thumbnail.OmdbClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;

@SpringBootTest
@Import({BaseTest.TestConfig.class})
@ActiveProfiles("test")
public abstract class BaseTest {
    // All tests that need access to the Spring context should extend this class.

    // Custom test beans to override/mock.
    @TestConfiguration
    public static class TestConfig {

        @Bean
        public OmdbClient omdbClient() {
            return mock(OmdbClient.class);
        }
    }
}