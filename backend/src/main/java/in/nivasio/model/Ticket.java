package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "tickets")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_status", def = "{'tenantId': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_tenant_dept", def = "{'tenantId': 1, 'department': 1}"),
        @CompoundIndex(name = "idx_tenant_created", def = "{'tenantId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_tenant_room", def = "{'tenantId': 1, 'roomNo': 1}"),
        @CompoundIndex(name = "idx_tenant_assigned", def = "{'tenantId': 1, 'assignedTo': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;

    @Indexed(unique = true)
    private String ticketId; // HK-0803-0001, LDY-0803-0042

    private String type; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, FOOD, GENERAL
    private String department;
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, DONE, CLOSED, REOPENED
    private String priority; // NORMAL, URGENT

    // Location
    private String roomNo;
    private String roomId;

    // People
    private String residentId;
    private String residentName;
    private String residentPhone;
    private String assignedTo;
    private String assignedToName;

    // Description & proof
    private String description;
    private List<String> photos;

    // SLA
    private Instant slaDeadline;
    private boolean slaBreach;
    private Instant escalatedAt;

    // WhatsApp
    private String waMessageId; // Dedup

    // Timestamps
    private Instant createdAt;
    private Instant assignedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant closedAt;
    private Instant updatedAt;

    // Recurring detection
    private boolean recurringFlag;
}
