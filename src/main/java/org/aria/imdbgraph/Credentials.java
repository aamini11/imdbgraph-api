package org.aria.imdbgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class Credentials {

    private final KeyVault keyVault;

    @Autowired
    public Credentials(KeyVault keyVault) {
        this.keyVault = keyVault;
    }

    @Bean
    public String omdbApiKey() {
        return keyVault.getSecret("omdbApiKey");
    }

    @Bean
    public String databasePassword() {
        return keyVault.getSecret("databasePassword");
    }
}
