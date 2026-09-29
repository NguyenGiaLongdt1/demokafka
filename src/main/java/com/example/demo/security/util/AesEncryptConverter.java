package com.example.demo.security.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JPA AttributeConverter dùng để tự động mã hóa AES-256 dữ liệu nhạy cảm
 * khi lưu vào PostgreSQL và tự động giải mã khi đọc lên Entity.
 * 
 * Cách dùng trên Entity:
 * {@code @Convert(converter = AesEncryptConverter.class)}
 * {@code private String cccdOrSecretData;}
 */
@Converter
@Component
public class AesEncryptConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] IV = "1234567890123456".getBytes(StandardCharsets.UTF_8); // 16 bytes IV

    // Khóa mã hóa AES 256-bit (32 bytes)
    private static String secretKeyString = "MySecretAesKeyForSensitiveData32";

    @Value("${app.security.aes-key:MySecretAesKeyForSensitiveData32}")
    public void setSecretKey(String key) {
        if (key != null && key.length() >= 16) {
            secretKeyString = key.substring(0, Math.min(key.length(), 32));
            while (secretKeyString.length() < 32) {
                secretKeyString += "0";
            }
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Loi khi ma hoa du lieu nhay cam", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decoded = Base64.getDecoder().decode(dbData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Neu du lieu cu chua bi ma hoa, tra ve nguyen ban de khong bi crash
            return dbData;
        }
    }
}
