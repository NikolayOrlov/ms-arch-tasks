package homework.arch.stockservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReserveRepository extends CrudRepository<ReserveEntity, UUID> {
    List<ReserveEntity> findAllByOrderId(UUID orderId);
    List<ReserveEntity> findAllByCartIdAndOrderIdIsNullAndReservationTimestampGreaterThan(UUID cartId, LocalDateTime reservationTimestamp);
    List<ReserveEntity> findAllByProductId(UUID productId);
}
