package in.nivasio.repository;

import in.nivasio.model.Resident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Resident> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    Page<Resident> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name, Pageable pageable);

    List<Resident> findByTenantIdAndRoomNoAndActiveTrue(String tenantId, String roomNo);

    List<Resident> findByTenantIdAndActiveTrue(String tenantId);
}
