package com.example.mhpractice.common.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptService {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String EncryptionAlgorithm = "AES/GCM/NoPadding";
    private static final SecureRandom secureRandom = new SecureRandom();

    // Encrypts the given plain text using AES-GCM algorithm and returns the Base64
    // encoded string.
    public String encryptUrlSafe(String plainText, SecretKey secretKey) {
        try {

            // Create IV (Initialization Vector)
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Create cipher instance
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm);

            // Create GCM parameter spec (specify auth tag length and IV)
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt plain text into cipherText
            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            // Combine cipherText and IV, so later decryption can use the same IV
            ByteBuffer cipherTextAndIV = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(cipherText)
                    .put(iv);

            // Return Base64 encoded string
            return Base64.getEncoder().encodeToString(cipherTextAndIV.array());

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt text.");
        }
    }

    public String decryptUrlSafe(String encryptedText, SecretKey secretKey) {
        try {
            // Decode Base64 encoded string
            byte[] cipherTextAndIV = Base64.getDecoder().decode(encryptedText);

            // Extract IV from the end of the cipherTextAndIV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(cipherTextAndIV, cipherTextAndIV.length - GCM_IV_LENGTH, iv, 0, GCM_IV_LENGTH);

            // Extract cipherText from the start of the cipherTextAndIV
            byte[] cipherText = new byte[cipherTextAndIV.length - GCM_IV_LENGTH];
            System.arraycopy(cipherTextAndIV, 0, cipherText, 0, cipherText.length);

            // Create cipher instance
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm);

            // Create GCM parameter spec (specify auth tag length and IV)
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt cipherText into plainText
            byte[] plainText = cipher.doFinal(cipherText);

            // Return plainText as String
            return new String(plainText);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt text.");
        }
    }
}
