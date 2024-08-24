package homework.arch.cartservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<CartEntity, UUID> {
    List<CartEntity> findAllByCustomerId(UUID customerId);
}
