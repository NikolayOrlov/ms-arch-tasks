package homework.arch.profileservice.rest;

import homework.arch.profileservice.persistence.UserDataEntity;
import homework.arch.profileservice.persistence.UserDataRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/public-api")
public class PublicApiController {
    private final UserDataRepository userDataRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDataDao> getProfile(HttpServletRequest request) {
        try {
            var userId = getAuthenticatedUserId(request);
            return ResponseEntity.ok(getUserProfileDataDao(getUserData(userId)));
        } catch (IllegalArgumentException ex) {
            log.debug("User unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody UserProfileDataDao dao, HttpServletRequest request) {
        try {
            var userId = getAuthenticatedUserId(request);
            var userData = getUserData(userId);
            updateUserData(userData, dao);
            userDataRepository.save(userData);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException ex) {
            log.debug("user unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private UUID getAuthenticatedUserId(HttpServletRequest request) {
        var id = UUID.fromString(Optional.ofNullable(request.getHeader("X-UserId"))
                                    .orElseThrow(IllegalArgumentException::new));
        log.debug("Authenticated userId = {}", id);
        return id;
    }

    private UserDataEntity getUserData(UUID userId) {
        var userDataOptional = userDataRepository.findById(userId);
        if (userDataOptional.isEmpty()) {
            log.debug("User {} not found", userId);
            throw new IllegalArgumentException();
        }
        return userDataOptional.get();
    }

    private UserProfileDataDao getUserProfileDataDao(UserDataEntity userData) {
        var dao = new UserProfileDataDao();
        dao.setAddress(userData.getAddress());
        dao.setEmail(userData.getEmail());
        dao.setName(userData.getName());
        dao.setPhone(userData.getPhone());
        return dao;
    }

    private void updateUserData(UserDataEntity userData, UserProfileDataDao dao) {
        if (nonNull(dao.getAddress())) {
            userData.setAddress(dao.getAddress());
        }
        if (nonNull(dao.getEmail())) {
            userData.setEmail(dao.getEmail());
        }
        if (nonNull(dao.getName())) {
            userData.setName(dao.getName());
        }
        if (nonNull(dao.getPhone())) {
            userData.setPhone(dao.getPhone());
        }
    }
}
