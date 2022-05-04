package com.kds.ourmemory.v1.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class EncryptUtilTest {

    @Autowired
    private EncryptUtil encryptUtil;

    @Test
    @DisplayName("양방향 암호화 테스트")
    void AES256Encrypt() {
        // given
        var plainText = "12345678";

        // when
        var encryptedText = encryptUtil.columnEncrypt(plainText);

        // then
        assertAll(
                () -> assertNotEquals(encryptedText, plainText),
                () -> assertEquals(plainText, encryptUtil.columnDecrypt(encryptedText))
        );
    }
}
