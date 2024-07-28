package homework.arch.authservice.rest;

import lombok.Data;
import lombok.SneakyThrows;

import java.beans.Transient;

import static homework.arch.authservice.util.PasswordHashUtil.getHash;

@Data
public class LoginCredentialsDao {
    private String login;
    private String password;

    @SneakyThrows
    @Transient
    public String getPasswordHash() {
        return getHash(password);
    }
}
