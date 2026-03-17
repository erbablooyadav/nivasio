package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_user_read", def = "{'tenantId': 1, 'userId': 1, 'read': 1}"),
        @CompoundIndex(name = "idx_tenant_created", def = "{'tenantId': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;

    @Indexed
    private String tenantId;
    private String userId; // Target user
    private String type; // TICKET_ASSIGNED, SLA_BREACH, SYSTEM, RENT_DUE
    private String title;
    private String message;
    private String referenceId; // e.g., ticketId, rentRecordId
    private boolean read;
    private Instant createdAt;
}
