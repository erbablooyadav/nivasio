package in.nivasio.repository;

import in.nivasio.model.Resident;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ResidentRepository extends MongoRepository<Resident, String> {
    List<Resident> findByTenantIdAndStatus(String tenantId, String status);

    List<Resident> findByTenantId(String tenantId);

    Optional<Resident> findByTenantIdAndPhone(String tenantId, String phone);

    Optional<Resident> findByPhone(String phone);

    Optional<Resident> findByIdAndTenantId(String id, String tenantId);

    long countByTenantId(String tenantId);
}
