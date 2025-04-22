package com.staticconstants.flowpad.backend.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static HashedPassword hashPassword(char[] password) throws Exception
    {
        byte[] salt = generateSalt();
        String hash = hashPassword(password, salt);
        Arrays.fill(password, '\0');
        return new HashedPassword(hash, Base64.getEncoder().encodeToString(salt));
    }

    public static boolean verifyPassword(char[] inputPassword, String storedHashBase64, String storedSaltBase64) throws Exception
    {
        byte[] salt = Base64.getDecoder().decode(storedSaltBase64);
        byte[] storedHash = Base64.getDecoder().decode(storedHashBase64);
        String inputHashBase64 = hashPassword(inputPassword, salt);
        byte[] inputHash = Base64.getDecoder().decode(inputHashBase64);
        Arrays.fill(inputPassword, '\0');
        return MessageDigest.isEqual(inputHash, storedHash);
    }

    private static String hashPassword(char[] password, byte[] salt) throws Exception
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private static byte[] generateSalt()
    {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }
}
