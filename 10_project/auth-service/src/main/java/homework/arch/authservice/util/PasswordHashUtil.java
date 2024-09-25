package homework.arch.authservice.util;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;

public interface PasswordHashUtil {
    @SneakyThrows
    static String getHash(String password) {
        if (!StringUtils.hasText(password)) {
            return null;
        }
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(password.getBytes());
        return new String(messageDigest.digest());
    }
}
