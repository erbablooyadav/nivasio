package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "food_feedback")
@CompoundIndex(name = "idx_tenant_created", def = "{'tenantId': 1, 'createdAt': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodFeedback {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String residentId;
    private String residentName;
    private String roomNo;
    private String category; // QUALITY, PORTION, HYGIENE, MENU_SUGGESTION, SPECIAL_REQUEST
    private String message;
    private int rating; // 1-5
    private String status; // PENDING, ACKNOWLEDGED, RESOLVED
    private String response;
    private String respondedBy;
    private Instant createdAt;
    private Instant resolvedAt;
}
