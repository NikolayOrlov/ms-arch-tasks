package homework.arch.notificationservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findAllByCustomerId(UUID customerId);
}
