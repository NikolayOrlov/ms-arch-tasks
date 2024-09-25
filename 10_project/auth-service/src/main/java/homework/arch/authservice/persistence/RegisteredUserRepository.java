package homework.arch.authservice.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RegisteredUserRepository extends CrudRepository<RegisteredUserEntity, UUID> {
    RegisteredUserEntity findBySessionId(UUID sessionId);
    RegisteredUserEntity findByLoginAndPasswordHash(String password, String passwordHash);
}
