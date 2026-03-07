package com.pgmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tickets")
@CompoundIndexes({
        @CompoundIndex(name = "tenant_status", def = "{'tenantId': 1, 'status': 1}"),
        @CompoundIndex(name = "tenant_created", def = "{'tenantId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "tenant_dept", def = "{'tenantId': 1, 'department': 1}"),
        @CompoundIndex(name = "tenant_assigned", def = "{'tenantId': 1, 'assignedTo': 1}")
})
public class Ticket {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed(unique = true)
    private String ticketId; // HK-0803-0001, LDY-0803-0002, etc.

    private String type; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, GENERAL
    private String department; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, FOOD
    private Status status;
    private Priority priority;

    private String roomId;
    private String roomNo;
    private String raisedByUserId;
    private String raisedByPhone;
    private String raisedByName;

    private String assignedTo; // Staff user ID
    private String assignedToName;

    private String description;
    private String notes;

    // SLA tracking
    private int slaThresholdMinutes;
    private boolean slaBreach;
    private Instant slaDeadline;

    // WhatsApp context
    private String waMessageId; // For deduplication

    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Instant closedAt;

    public enum Status {
        OPEN, IN_PROGRESS, DONE, CLOSED
    }

    public enum Priority {
        NORMAL, URGENT
    }
}
