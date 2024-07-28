package homework.arch.authservice.rest;

import lombok.Data;

@Data
public class RegistrationDataDao {
    private String login;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;
}
