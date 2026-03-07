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
@Document(collection = "food_feedback")
@CompoundIndex(name = "tenant_created", def = "{'tenantId': 1, 'createdAt': -1}")
public class FoodFeedback {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String userId;
    private String userName;
    private String roomNo;

    private Category category; // COMPLAINT, FEEDBACK, SPECIAL_REQUEST
    private String message;
    private String status; // OPEN, ACKNOWLEDGED, RESOLVED

    private Instant createdAt;
    private Instant resolvedAt;

    public enum Category {
        COMPLAINT, FEEDBACK, SPECIAL_REQUEST
    }
}
