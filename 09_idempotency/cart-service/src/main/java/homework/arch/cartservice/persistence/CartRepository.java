package homework.arch.cartservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<CartEntity, UUID> {

    Optional<CartEntity> findByCustomerIdAndStatus(UUID customerId, CartEntity.CartStatus status);
}
