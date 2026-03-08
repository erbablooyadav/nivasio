package in.nivasio.repository;

import in.nivasio.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TenantRepository extends MongoRepository<Tenant, String> {
    Optional<Tenant> findByTenantId(String tenantId);

    Optional<Tenant> findByOwnerPhone(String phone);

    Optional<Tenant> findByOwnerEmail(String email);

    boolean existsByOwnerPhone(String phone);
}
