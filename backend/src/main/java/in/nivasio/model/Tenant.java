package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private String name;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String address;

    // Subscription
    private String plan; // FREE, STARTER, GROWTH, PRO, ENTERPRISE
    private Instant planExpiry;

    // WhatsApp Configuration
    private WhatsAppConfig waConfig;

    // Razorpay Configuration
    private RazorpayConfig razorpayConfig;

    // SLA Configuration (per department — key = department name)
    private Map<String, Integer> slaConfig;

    // Feature flags
    private List<String> features;

    // Multi-language
    private String defaultLanguage;

    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppConfig {
        private String businessId;
        private String phoneNumberId;
        private String accessToken;
        private String verifyToken;
        private boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RazorpayConfig {
        private String keyId;
        private String keySecret;
        private boolean enabled;
    }
}
