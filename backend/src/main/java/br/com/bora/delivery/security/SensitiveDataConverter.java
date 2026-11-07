package br.com.bora.delivery.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class SensitiveDataConverter implements AttributeConverter<String, String> {
    private static final String PREFIX = "enc:v1:";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final SecretKey KEY = loadKey();

    @Override
    public String convertToDatabaseColumn(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + ciphertext.length)
                    .put(nonce).put(ciphertext).array());
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("Não foi possível proteger os dados pessoais.", error);
        }
    }

    @Override
    public String convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) return null;
        if (!value.startsWith(PREFIX)) {
            throw new IllegalStateException("Campo pessoal armazenado sem criptografia; interrompendo leitura segura.");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= NONCE_LENGTH) throw new IllegalArgumentException("Payload criptográfico inválido.");
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] nonce = new byte[NONCE_LENGTH];
            buffer.get(nonce);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            throw new IllegalStateException("Não foi possível descriptografar os dados pessoais.", error);
        }
    }

    private static SecretKey loadKey() {
        String encodedKey = System.getenv("BORA_PII_ENCRYPTION_KEY");
        if (encodedKey != null && !encodedKey.isBlank()) {
            byte[] key = Base64.getDecoder().decode(encodedKey);
            if (key.length != 32) {
                throw new IllegalStateException("BORA_PII_ENCRYPTION_KEY deve ser uma chave Base64 de 32 bytes.");
            }
            return new SecretKeySpec(key, "AES");
        }
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("Não foi possível inicializar a proteção de dados pessoais.", error);
        }
    }
}
