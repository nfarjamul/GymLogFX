package com.gymlog.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * Minimal local "account" store backing the login screen: one username
 * and a salted SHA-256 password hash, persisted to a small properties
 * file under the user's home directory. This gives GymLogFX a real
 * login gate for a single local user - it is not a substitute for a
 * proper authentication backend (no encryption at rest, no multi-user
 * support, no password reset flow).
 */
public class UserStore {

    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".gymlogfx", "user.properties");

    public synchronized boolean hasAccount() {
        return Files.exists(FILE);
    }

    public synchronized String getUsername() {
        return load().getProperty("username");
    }

    public synchronized void createAccount(String username, String password) {
        String salt = generateSalt();
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("salt", salt);
        props.setProperty("hash", hash(password, salt));
        save(props);
    }

    public synchronized boolean verify(String username, String password) {
        Properties props = load();
        String storedUser = props.getProperty("username");
        String salt = props.getProperty("salt");
        String storedHash = props.getProperty("hash");
        if (storedUser == null || salt == null || storedHash == null) {
            return false;
        }
        return storedUser.equals(username) && storedHash.equals(hash(password, salt));
    }

    private Properties load() {
        Properties props = new Properties();
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[UserStore] Failed to load account: " + e.getMessage());
            }
        }
        return props;
    }

    private void save(Properties props) {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "GymLogFX local account - not for production use");
            }
        } catch (IOException e) {
            System.err.println("[UserStore] Failed to save account: " + e.getMessage());
        }
    }

    private static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            return toHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
