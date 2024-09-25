package homework.arch.profileservice.rest;

import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.profileservice.api.dto.generated.CustomerDto;
import homework.arch.profileservice.api.generated.CustomerApi;
import homework.arch.profileservice.mapper.Mapper;
import homework.arch.profileservice.persistence.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CustomerApiImpl implements CustomerApi {
    private final UserDataRepository userDataRepository;
    private final Mapper mapper;

    @Override
    @ExecutionMonitoring
    public ResponseEntity<Void> submitCustomerData(CustomerDto customerDto) {
        log.debug("submit user data: {}", customerDto);
        var userDataEntity = mapper.toEntity(customerDto);
        userDataRepository.save(userDataEntity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
