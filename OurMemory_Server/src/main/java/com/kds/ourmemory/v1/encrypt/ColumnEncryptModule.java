package com.kds.ourmemory.v1.encrypt;

import com.kds.ourmemory.v1.config.CustomConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;

@RequiredArgsConstructor
@Component
public class ColumnEncryptModule {

    private final CustomConfig customConfig;

    private final Charset charset = StandardCharsets.UTF_8;

    public String encrypt(String plainText) throws Exception {
        SecretKey keyspec = new SecretKeySpec(customConfig.getAes256Key().getBytes(charset), "AES");

        //set iv as random 16byte
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        AlgorithmParameterSpec ivspec = new IvParameterSpec(iv);

        // Encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

        int blockSize = 128; //block size
        byte[] dataBytes = plainText.getBytes(charset);

        //find fillChar & pad
        int plaintextLength = dataBytes.length;
        int fillChar = ((blockSize - (plaintextLength % blockSize)));
        plaintextLength += fillChar; //pad

        byte[] plaintext = new byte[plaintextLength];
        Arrays.fill(plaintext, (byte) fillChar);
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

        //encrypt
        byte[] cipherBytes = cipher.doFinal(plaintext);

        //add iv to front of cipherBytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( iv );
        outputStream.write( cipherBytes );

        //encode into base64
        byte [] encryptedIvText = outputStream.toByteArray();
        return new String(Base64.getEncoder().encode(encryptedIvText), charset);
    }

    public String decrypt(String encryptedText) throws Exception {
        //decode with base64 decoder
        byte [] encryptedIvTextBytes = Base64.getDecoder().decode(encryptedText);

        // Extract IV.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);



        // Decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey keyspec = new SecretKeySpec(customConfig.getAes256Key().getBytes(charset), "AES");
        AlgorithmParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        byte[] aesdecode = cipher.doFinal(encryptedBytes);

        // unpad
        byte[] origin = new byte[aesdecode.length - (aesdecode[aesdecode.length - 1])];
        System.arraycopy(aesdecode, 0, origin, 0, origin.length);

        return new String(origin, charset);
    }

}
