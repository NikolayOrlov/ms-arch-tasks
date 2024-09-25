package homework.arch.authservice.rest;

import homework.arch.authservice.api.dto.generated.LoginDto;
import homework.arch.authservice.api.dto.generated.RegistrationDto;
import homework.arch.authservice.api.generated.DefaultApi;
import homework.arch.authservice.client.order.dto.generated.CustomerDto;
import homework.arch.authservice.client.order.generated.CustomerApiClient;
import homework.arch.authservice.persistence.RegisteredUserEntity;
import homework.arch.authservice.persistence.RegisteredUserRepository;
import homework.arch.monitoring.ExecutionMonitoring;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

import static java.util.Objects.isNull;
import static homework.arch.authservice.util.PasswordHashUtil.getHash;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthApiImpl implements DefaultApi {
    private static final String SESSIONID = "SESSIONID";
    private static final String SUCCESS = "SUCCESS";

    private final RegisteredUserRepository registeredUserRepository;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final CustomerApiClient customerApiClient;

    @Override
    @ExecutionMonitoring
    public ResponseEntity<String> register(RegistrationDto registrationDto) {
        var registeredUser = newRegisteredUser(registrationDto);
        sendProfileData(registrationDto, registeredUser);
        log.debug("registered: {}", registrationDto);
        return ResponseEntity.ok(registeredUser.getId().toString());
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<String> login(LoginDto loginDto) {
        var user = registeredUserRepository.findByLoginAndPasswordHash(loginDto.getLogin(), getHash(loginDto.getPassword()));
        if (isNull(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            UUID sessionId = newSessionId(user);
            httpServletResponse.addCookie(new Cookie(SESSIONID, sessionId.toString()));
            return ResponseEntity.ok(SUCCESS);
        }
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<Void> logout() {
        var sessionId = getSessionIdCookie(httpServletRequest);
        if (!isNull(sessionId)) {
            var user = registeredUserRepository.findBySessionId(UUID.fromString(sessionId));
            if (!isNull(user)) {
                user.setSessionId(null);
                registeredUserRepository.save(user);
                log.debug("logout: {} / {}", sessionId, user.getId());
            }
            Cookie sessionIdCookie = new Cookie(SESSIONID, "");
            sessionIdCookie.setMaxAge(0);
            httpServletResponse.addCookie(sessionIdCookie);
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<String> auth() {
        var sessionId = getSessionIdCookie(httpServletRequest);
        if (!isNull(sessionId)) {
            var user = registeredUserRepository.findBySessionId(UUID.fromString(sessionId));
            if (!isNull(user)) {
                log.debug("auth ok: {} / {}", sessionId, user.getId());
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("X-UserId", user.getId().toString());
                responseHeaders.set("X-User", user.getLogin());
                return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(user.getId().toString());
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

    private RegisteredUserEntity newRegisteredUser(RegistrationDto registrationDto) {
        var registeredUser = new RegisteredUserEntity();
        registeredUser.setLogin(registrationDto.getLogin());
        registeredUser.setPasswordHash(getHash(registrationDto.getPassword()));
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

    private void sendProfileData(RegistrationDto registrationDto, RegisteredUserEntity registeredUser) {
        var customerDto = new CustomerDto();
        customerDto.setId(registeredUser.getId());
        customerDto.setName(registrationDto.getName());
        customerDto.setAddress(registrationDto.getAddress());
        customerDto.setEmail(registrationDto.getEmail());
        customerDto.setPhone(registrationDto.getPhone());
        customerApiClient.submitCustomerData(customerDto);
        log.debug("sent profile data: {}", customerDto);
    }
}
