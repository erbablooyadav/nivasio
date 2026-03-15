package in.nivasio.repository;

import in.nivasio.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityType(String tenantId, String entityType, Pageable pageable);

    Page<AuditLog> findByTenantIdAndAction(String tenantId, String action, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeAndAction(String tenantId, String entityType, String action,
            Pageable pageable);

    List<AuditLog> findByTenantIdAndEntityId(String tenantId, String entityId);

    long countByTenantIdAndTimestampAfter(String tenantId, Instant after);
}
