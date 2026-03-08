package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_log")
@CompoundIndex(name = "idx_tenant_entity", def = "{'tenantId': 1, 'entityType': 1, 'entityId': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private String id;
    private String tenantId;
    private String entityType; // TICKET, PAYMENT, LOGIN, STAFF, RESIDENT, CONFIG
    private String entityId;
    private String action; // CREATED, UPDATED, DELETED, STATUS_CHANGED, VIEWED
    private String performedBy;
    private String performerRole;
    private String ip;
    private String userAgent;
    private String oldValue;
    private String newValue;
    private Instant timestamp;
}
