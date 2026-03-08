package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * WhatsApp bot conversation state — TTL 30 min.
 */
@Document(collection = "bot_sessions")
@CompoundIndex(name = "idx_tenant_phone", def = "{'tenantId': 1, 'whatsappNo': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotSession {
    @Id
    private String id;
    private String tenantId;
    private String whatsappNo;
    private String currentStep; // GREETING, SERVICE_SELECTION, ROOM_CONFIRMATION, COMPLETE
    private Map<String, String> context;
    private Instant lastActivity;

    @Indexed(expireAfter = "30m")
    private Instant expiresAt;
}
