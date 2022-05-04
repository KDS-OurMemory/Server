package com.kds.ourmemory.v1.util;

import com.kds.ourmemory.v1.encrypt.ColumnEncryptModule;
import com.kds.ourmemory.v1.encrypt.EncryptTarget;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EncryptUtil {

    private ColumnEncryptModule columnEncryptModule;

    public String columnEncrypt(String plainText) {
        return encrypt(plainText, EncryptTarget.COLUMN);
    }

    public String columnDecrypt(String encryptedText) {
        return decrypt(encryptedText, EncryptTarget.COLUMN);
    }

    private String encrypt(String plainText, EncryptTarget target) {
        return switch (target) {
            case COLUMN -> columnEncryptModule.encrypt(plainText);
            default -> plainText;
        };
    }

    private String decrypt(String encryptedText, EncryptTarget target) {
        return switch (target) {
            case COLUMN -> columnEncryptModule.decrypt(encryptedText);
            default -> encryptedText;
        };
    }

}
