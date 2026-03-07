package com.pgmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "subscriptions")
public class Subscription {
    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private String plan; // FREE, BASIC, STANDARD, PRO, ENTERPRISE
    private String razorpaySubId;
    private String razorpayCustomerId;
    private String paymentStatus; // ACTIVE, PENDING, CANCELLED, EXPIRED

    private int maxRooms;
    private int maxDepartments;

    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;
}
