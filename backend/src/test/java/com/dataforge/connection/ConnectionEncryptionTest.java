package com.dataforge.connection;

import com.dataforge.config.EncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConnectionEncryptionTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    void encryptAndDecryptRoundtrip() {
        String original = "mySecretPassword123!";
        String encrypted = encryptionService.encrypt(original);
        assertThat(encrypted).isNotEqualTo(original);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void eachEncryptionProducesDifferentCiphertext() {
        String password = "fixedPassword";
        String first = encryptionService.encrypt(password);
        String second = encryptionService.encrypt(password);
        assertThat(first).isNotEqualTo(second);  // IV is random per encryption
    }

    @Test
    void decryptInvalidDataThrows() {
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> encryptionService.decrypt("not-valid-base64"));
    }
}
