package homework.arch.checkoutservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountReplenishmentRepository extends CrudRepository<AccountReplenishmentEntity, UUID> {
    List<AccountReplenishmentEntity> findAllByAccountId(UUID accountId);
}
