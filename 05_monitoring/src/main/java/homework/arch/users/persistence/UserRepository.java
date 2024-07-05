package homework.arch.users.persistence;

import homework.arch.users.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.UUID;

@RepositoryRestResource
public interface UserRepository extends CrudRepository<User, UUID> {

}
