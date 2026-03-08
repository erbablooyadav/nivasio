package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    private String id;
    private String tenantId;
    private String plan; // FREE, STARTER, GROWTH, PRO, ENTERPRISE
    private String razorpaySubId;
    private Instant startDate;
    private Instant endDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED
    private boolean autoRenew;
    private Instant createdAt;
}
