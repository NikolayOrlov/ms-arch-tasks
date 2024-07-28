package homework.arch.profileservice.rest;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString(callSuper=true)
public class UserDataDao extends UserProfileDataDao {
    private UUID id;
}
