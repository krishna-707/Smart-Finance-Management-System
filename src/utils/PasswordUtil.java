package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    public static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String hash(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] value = (salt + password).getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 120000; i++) value = md.digest(value);
        return Base64.getEncoder().encodeToString(value);
    }

    public static boolean verify(String password, String salt, String expected) throws Exception {
        return MessageDigest.isEqual(
            hash(password, salt).getBytes(StandardCharsets.UTF_8),
            expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
