package br.com.bora.delivery.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Base64;

@Configuration
public class PiiEncryptionConfiguration {
    public PiiEncryptionConfiguration(Environment environment) {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod")
                        || profile.equalsIgnoreCase("production"));
        String encodedKey = System.getenv("BORA_PII_ENCRYPTION_KEY");
        if (production && (encodedKey == null || encodedKey.isBlank())) {
            throw new IllegalStateException("Configure BORA_PII_ENCRYPTION_KEY no gerenciador de segredos antes de iniciar.");
        }
        if (production) {
            byte[] key = Base64.getDecoder().decode(encodedKey);
            if (key.length != 32) {
                throw new IllegalStateException("BORA_PII_ENCRYPTION_KEY deve ser uma chave Base64 de 32 bytes.");
            }
        }
    }
}
