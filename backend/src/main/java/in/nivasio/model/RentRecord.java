package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rent_records")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_status", def = "{'tenantId': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_tenant_resident_month", def = "{'tenantId': 1, 'residentId': 1, 'month': 1}"),
        @CompoundIndex(name = "idx_tenant_due", def = "{'tenantId': 1, 'dueDate': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentRecord {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String residentId;
    private String residentName;
    private String roomNo;

    private double amount;
    private String month; // "2026-03" format
    private Instant dueDate;
    private Instant paidDate;

    private String status; // PENDING, PAID, OVERDUE, PARTIAL
    private String paymentMode; // CASH, UPI, BANK, RAZORPAY
    private String transactionId;
    private String receiptUrl;

    private boolean reminderSent;
    private Instant createdAt;
}
