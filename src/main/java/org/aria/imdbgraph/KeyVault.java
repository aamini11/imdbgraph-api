package org.aria.imdbgraph;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeyVault {

    private final SecretClient client;

    @Autowired
    public KeyVault(
            @Value("${azure.keyvault.vault-url}") String vaultUrl,
            @Value("${azure.keyvault.tenant-id}") String tenantId,
            @Value("${azure.keyvault.client-id}") String clientId,
            @Value("${azure.keyvault.client-secret}") String clientSecret
    ) {
        this.client = new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new ClientSecretCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build())
                .buildClient();
    }

    public String getSecret(String key) {
        return client.getSecret(key).getValue();
    }
}