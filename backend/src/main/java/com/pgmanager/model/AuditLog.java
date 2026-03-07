package com.pgmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_log")
@CompoundIndex(name = "tenant_ticket", def = "{'tenantId': 1, 'ticketId': 1}")
public class AuditLog {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String ticketId;
    private String changedBy; // User ID
    private String changedByName;
    private String action; // STATUS_CHANGE, ASSIGNMENT, CREATED, etc.
    private String oldValue;
    private String newValue;
    private String ipAddress;

    private Instant timestamp;
}
