package com.staticconstants.flowpad.backend.security;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static HashedPassword hashPassword(char[] password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return new HashedPassword(Base64.getEncoder().encodeToString(hash),
                    Base64.getEncoder().encodeToString(salt));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            // Securely zero the password after use
            java.util.Arrays.fill(password, '\0');
        }
    }

    public static boolean verifyPassword(char[] password, String hashBase64, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] expectedHash = Base64.getDecoder().decode(hashBase64);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = skf.generateSecret(spec).getEncoded();

            if (actualHash.length != expectedHash.length) return false;

            for (int i = 0; i < actualHash.length; i++) {
                if (actualHash[i] != expectedHash[i]) return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
