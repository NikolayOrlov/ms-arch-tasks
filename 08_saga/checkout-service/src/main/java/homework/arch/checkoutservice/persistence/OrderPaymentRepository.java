package homework.arch.checkoutservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderPaymentRepository extends CrudRepository<OrderPaymentEntity, UUID> {
    List<OrderPaymentEntity> findAllByAccountId(UUID accountId);
}
