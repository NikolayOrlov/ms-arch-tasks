package homework.arch.profileservice.rest;

import homework.arch.exception.NotFoundException;
import homework.arch.profileservice.api.dto.generated.CustomerProfileDto;
import homework.arch.profileservice.api.generated.ProfileApi;
import homework.arch.profileservice.mapper.Mapper;
import homework.arch.profileservice.persistence.UserDataEntity;
import homework.arch.profileservice.persistence.UserDataRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static homework.arch.token.TokenExtractor.getAuthenticatedUserId;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProfileApiImpl implements ProfileApi {
    private final UserDataRepository userDataRepository;
    private final HttpServletRequest httpServletRequest;
    private final Mapper mapper;

    @Override
    public ResponseEntity<CustomerProfileDto> getProfile(UUID xUserId) {
        var userId = getAuthenticatedUserId(httpServletRequest);
        return ResponseEntity.ok(mapper.toDto((getUserData(userId))));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateProfile(UUID xUserId, CustomerProfileDto customerProfileDto) {
        var userId = getAuthenticatedUserId(httpServletRequest);
        var userData = getUserData(userId);
        mapper.updateEntity(userData, customerProfileDto);
        userDataRepository.save(userData);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UserDataEntity getUserData(UUID userId) {
        return userDataRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId.toString()));
    }
}
