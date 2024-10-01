package homework.arch.productservice.persistence;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends ListCrudRepository<ProductEntity, UUID> {
}
