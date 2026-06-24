package com.dataforge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.net.URI;
import java.util.Map;

/**
 * ponytail: Vault integration for connection secrets.
 * Falls back to local encryption when vault.enabled=false (default).
 * Upgrade: add AppRole auth for production.
 */
@Configuration
@ConditionalOnProperty(name = "vault.enabled", havingValue = "true")
public class VaultConfig {

    @Bean
    public VaultTemplate vaultTemplate(
            @Value("${vault.uri:http://localhost:8200}") String uri,
            @Value("${vault.token:fingerseal-dev-token}") String token) {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(uri));
        return new VaultTemplate(endpoint, new TokenAuthentication(token));
    }

    /**
     * Fetches a connection's credentials from Vault KV store.
     * Returns null if the path doesn't exist in Vault.
     */
    public static Map<String, Object> fetchCredentials(VaultTemplate vault, String mountPath, String path) {
        VaultResponseSupport<Map> response = vault
                .opsForKeyValue(mountPath, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2)
                .get(path, Map.class);
        if (response == null || response.getData() == null) return null;
        return response.getData();
    }
}
