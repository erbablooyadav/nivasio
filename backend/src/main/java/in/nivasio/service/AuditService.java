package in.nivasio.service;

import in.nivasio.model.AuditLog;
import in.nivasio.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * APPEND-ONLY audit log — never update, never delete.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;

    public void log(String tenantId, String entityType, String entityId,
            String action, String performedBy, String details) {
        AuditLog entry = AuditLog.builder()
                .tenantId(tenantId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .newValue(details)
                .timestamp(Instant.now())
                .build();
        auditRepo.save(entry);
    }
}
