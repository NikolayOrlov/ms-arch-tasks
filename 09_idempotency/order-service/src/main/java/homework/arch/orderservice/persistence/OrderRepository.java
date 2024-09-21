package homework.arch.orderservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends CrudRepository<OrderEntity, UUID> {
    List<OrderEntity> findAllByCustomerId(UUID customerId);
}
