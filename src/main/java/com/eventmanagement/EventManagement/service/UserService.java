package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.configuration.CacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private CacheRepository cacheRepository;

    public SecretKey getKeyFromCache(String uniqueId) {
        try {
            return cacheRepository.getKey(uniqueId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public String encrypt(String plainText, SecretKey key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = null;
            encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }



    public String decrypt(String encryptedText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[12];
            System.arraycopy(decodedBytes, 0, iv, 0, 12);

            byte[] encryptedData = new byte[decodedBytes.length - 12];
            System.arraycopy(decodedBytes, 12, encryptedData, 0, encryptedData.length);

            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            System.out.println("Password inside decryption method: "+new String(decryptedBytes));

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 input: " + e.getMessage());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.println("Encryption algorithm/padding not supported: " + e.getMessage());
        } catch (InvalidKeyException e) {
            System.err.println("Invalid secret key provided: " + e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            System.err.println("Invalid GCM parameter: " + e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            System.err.println("Decryption error: Invalid data or incorrect key: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during decryption: " + e.getMessage());
        }
        return null; // Return null or handle appropriately if decryption fails
    }



}
