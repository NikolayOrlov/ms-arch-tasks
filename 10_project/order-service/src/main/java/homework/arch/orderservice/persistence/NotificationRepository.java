package homework.arch.orderservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {
    Optional<NotificationEntity> findFirstBySendStatus(NotificationEntity.SendStatus status);
}
