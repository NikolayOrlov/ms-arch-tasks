package homework.arch.notificationservice.rest;

import homework.arch.notificationservice.api.dto.generated.NotificationDto;
import homework.arch.notificationservice.api.generated.NotificationApi;
import homework.arch.notificationservice.mapper.Mapper;
import homework.arch.notificationservice.persistence.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationApiImpl implements NotificationApi {
    private final NotificationRepository notificationRepository;
    private final Mapper mapper;

    @Override
    public ResponseEntity<List<NotificationDto>> getNotifications(UUID customerId) {
        return ResponseEntity.ok(notificationRepository.findAllByCustomerId(customerId)
                                    .stream().map(mapper::toDto).toList());
    }

    @Override
    public ResponseEntity<Void> notifyCustomer(NotificationDto notificationDto) {
        notificationRepository.save(mapper.toDomain(notificationDto));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}