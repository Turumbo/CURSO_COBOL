package br.com.bora.delivery.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensitiveDataConverterTest {
    private final SensitiveDataConverter converter = new SensitiveDataConverter();

    @Test
    void encryptsAndDecryptsPersonalData() {
        String plaintext = "Rua das Flores, 42";

        String stored = converter.convertToDatabaseColumn(plaintext);

        assertThat(stored).startsWith("enc:v1:");
        assertThat(stored).doesNotContain(plaintext);
        assertThat(converter.convertToEntityAttribute(stored)).isEqualTo(plaintext);
    }

    @Test
    void refusesToReadLegacyPlaintextPersonalData() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Rua antiga, 10"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem criptografia");
    }
}
