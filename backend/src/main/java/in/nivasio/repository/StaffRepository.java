package in.nivasio.repository;

import in.nivasio.model.Staff;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends MongoRepository<Staff, String> {
    List<Staff> findByTenantIdAndActiveTrue(String tenantId);

    List<Staff> findByTenantIdAndDepartmentAndActiveTrue(String tenantId, String department);

    Optional<Staff> findByIdAndTenantId(String id, String tenantId);

    Optional<Staff> findByTenantIdAndPhone(String tenantId, String phone);

    Optional<Staff> findByPhone(String phone);

    long countByTenantIdAndActiveTrue(String tenantId);
}
