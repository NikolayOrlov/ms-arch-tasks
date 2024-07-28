package homework.arch.authservice.gateway;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDataDao {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
