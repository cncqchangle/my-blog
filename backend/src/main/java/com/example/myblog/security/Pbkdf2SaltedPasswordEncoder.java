package com.example.myblog.security;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Pbkdf2SaltedPasswordEncoder implements PasswordEncoder {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10_000;
    private static final int KEY_LENGTH = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = derive(rawPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.contains(":")) {
            return false;
        }
        String[] parts = encodedPassword.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = derive(rawPassword, salt);
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    private byte[] derive(CharSequence rawPassword, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec = new PBEKeySpec(rawPassword.toString().toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return factory.generateSecret(keySpec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }
}
