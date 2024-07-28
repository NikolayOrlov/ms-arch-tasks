package homework.arch.profileservice.rest;

import lombok.Data;

@Data
public class UserProfileDataDao {
    private String name;
    private String email;
    private String phone;
    private String address;
}
