package homework.arch.orderservice.transactionaloutbox;

import homework.arch.orderservice.mapper.Mapper;
import homework.arch.orderservice.persistence.NotificationEntity;
import homework.arch.orderservice.persistence.NotificationRepository;
import homework.arch.orderservice.client.notification.dto.generated.NotificationDto;
import homework.arch.orderservice.client.notification.generated.NotificationApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageRelay {
    // TODO: to implement it concurrency safe
    private final int SEND_OUT_PERIOD_EVERY_MINUTE_IN_MS = 1000 * 60;
    private final NotificationRepository notificationRepository;
    private final NotificationApiClient notificationApiClient;
    private final Mapper mapper;

    @Transactional
    public void scheduleSendOut(NotificationDto notification) {
        notificationRepository.save(mapper.toDomain(notification).setSendStatus(NotificationEntity.SendStatus.TO_BE_SEND));
        log.debug("Scheduled message '{}' to customer {}", notification.getMessage(), notification.getCustomerId());
    }

    @Scheduled(fixedDelay = SEND_OUT_PERIOD_EVERY_MINUTE_IN_MS)
    @Transactional
    public void sendOut() {
        var toBeSend = notificationRepository.findFirstBySendStatus(NotificationEntity.SendStatus.TO_BE_SEND);
        if (toBeSend.isPresent()) {
            var notification = toBeSend.get();
            notificationApiClient.notifyCustomer(UUID.randomUUID(), mapper.toDto(notification));
            log.info("Sent message '{}' to customer {}", notification.getMessage(), notification.getCustomerId());
            notification.setSendStatus(NotificationEntity.SendStatus.SENT);
            notificationRepository.save(notification);
        }
    }
}
