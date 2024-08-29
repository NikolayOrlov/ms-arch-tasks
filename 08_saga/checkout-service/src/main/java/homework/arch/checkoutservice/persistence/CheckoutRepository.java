package homework.arch.checkoutservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckoutRepository extends CrudRepository<CheckoutEntity, UUID> {

}
