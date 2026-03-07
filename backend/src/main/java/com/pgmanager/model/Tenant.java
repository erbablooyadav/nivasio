package com.pgmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tenants")
public class Tenant {
    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private String name;
    private String address;
    private String contactPhone;
    private String contactEmail;

    // WhatsApp configuration
    private WhatsAppConfig whatsappConfig;

    // Subscription plan
    private String plan; // FREE, BASIC, STANDARD, PRO, ENTERPRISE

    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WhatsAppConfig {
        private String businessId;
        private String accessToken;
        private String phoneNumberId;
        private String verifyToken;
        private String webhookSecret;
    }
}
