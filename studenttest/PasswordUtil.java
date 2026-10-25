
package com.studenttest;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        try {
            byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();

            return ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                "Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(
            String password, String storedValue) {

        if (password == null || storedValue == null) {
            return false;
        }

        String[] parts = storedValue.split(":");

        if (parts.length != 3) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[0]);

            if (iterations < 100000 || iterations > 2000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);

            if (salt.length != 16 || expected.length != 32) {
                return false;
            }

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt,
                iterations, expected.length * 8);

            try {
                byte[] actual = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();

                return java.security.MessageDigest
                    .isEqual(expected, actual);
            } finally {
                spec.clearPassword();
            }

        } catch (IllegalArgumentException
                 | GeneralSecurityException e) {
            return false;
        }
    }
}
