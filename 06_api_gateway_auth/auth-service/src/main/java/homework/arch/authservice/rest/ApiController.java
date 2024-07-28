package homework.arch.authservice.rest;

import homework.arch.authservice.persistence.RegisteredUserEntity;
import homework.arch.authservice.gateway.ProfileServiceGateway;
import homework.arch.authservice.gateway.UserDataDao;
import homework.arch.authservice.persistence.RegisteredUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

import static homework.arch.authservice.util.PasswordHashUtil.getHash;
import static java.util.Objects.isNull;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ApiController {
    private static final String SESSIONID = "SESSIONID";
    private static final String SUCCESS = "SUCCESS";
    private final RegisteredUserRepository registeredUserRepository;
    private final ProfileServiceGateway profileServiceGateway;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<String> login(@RequestBody LoginCredentialsDao loginCredentials, HttpServletResponse response) {
        var user = registeredUserRepository.findByLoginAndPasswordHash(loginCredentials.getLogin(),
                                                                       loginCredentials.getPasswordHash());
        if (isNull(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            UUID sessionId = newSessionId(user);
            response.addCookie(new Cookie(SESSIONID, sessionId.toString()));
            return ResponseEntity.ok(SUCCESS);
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UUID> register(@RequestBody RegistrationDataDao registrationData) {
        var registeredUser = newRegisteredUser(registrationData);
        sendProfileData(registrationData, registeredUser);
        log.debug("registered: {}", registrationData);
        return ResponseEntity.ok(registeredUser.getId());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var sessionId = getSessionIdCookie(request);
        if (!isNull(sessionId)) {
            var user = registeredUserRepository.findBySessionId(UUID.fromString(sessionId));
            if (!isNull(user)) {
                user.setSessionId(null);
                registeredUserRepository.save(user);
                log.debug("logout: {} / {}", sessionId, user.getId());
            }
            Cookie sessionIdCookie = new Cookie(SESSIONID, "");
            sessionIdCookie.setMaxAge(0);
            response.addCookie(sessionIdCookie);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping("/auth")
    public ResponseEntity<UUID> register(HttpServletRequest request) {
        var sessionId = getSessionIdCookie(request);
        if (!isNull(sessionId)) {
            var user = registeredUserRepository.findBySessionId(UUID.fromString(sessionId));
            if (!isNull(user)) {
                log.debug("auth ok: {} / {}", sessionId, user.getId());
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("X-UserId", user.getId().toString());
                responseHeaders.set("X-User", user.getLogin());
                return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(user.getId());
            }
        }
        log.debug("auth unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private UUID newSessionId(RegisteredUserEntity registeredUser) {
        UUID sessionId = UUID.randomUUID();
        registeredUser.setSessionId(sessionId);
        registeredUserRepository.save(registeredUser);
        return sessionId;
    }

    private void sendProfileData(RegistrationDataDao registrationData, RegisteredUserEntity registeredUser) {
        var userData = new UserDataDao();
        userData.setId(registeredUser.getId());
        userData.setName(registrationData.getName());
        userData.setAddress(registrationData.getAddress());
        userData.setEmail(registrationData.getEmail());
        userData.setPhone(registrationData.getPhone());
        profileServiceGateway.sendUserData(userData);
        log.debug("sent profile data: {}", userData);
    }

    private RegisteredUserEntity newRegisteredUser(RegistrationDataDao registrationData) {
        var registeredUser = new RegisteredUserEntity();
        registeredUser.setLogin(registrationData.getLogin());
        registeredUser.setPasswordHash(getHash(registrationData.getPassword()));
        registeredUser = registeredUserRepository.save(registeredUser);
        return registeredUser;
    }

    private String getSessionIdCookie(HttpServletRequest request) {
        if (!isNull(request.getCookies())) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> SESSIONID.equals(c.getName()))
                    .findAny()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }
}
