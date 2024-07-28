package homework.arch.profileservice.rest;

import homework.arch.profileservice.persistence.UserDataEntity;
import homework.arch.profileservice.persistence.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/private-api")
public class InternalApiController {
    private final UserDataRepository userDataRepository;

    @PostMapping("/user")
    public ResponseEntity<Void> submitUserData(@RequestBody UserDataDao userData) {
        log.debug("submit user data: {}", userData);
        var userDataEntity = getUserDataEntity(userData);
        userDataRepository.save(userDataEntity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private UserDataEntity getUserDataEntity(UserDataDao userData) {
        var userDataEntity = new UserDataEntity();
        userDataEntity.setId(userData.getId());
        userDataEntity.setAddress(userData.getAddress());
        userDataEntity.setEmail(userData.getEmail());
        userDataEntity.setName(userData.getName());
        userDataEntity.setPhone(userData.getPhone());
        return userDataEntity;
    }
}
