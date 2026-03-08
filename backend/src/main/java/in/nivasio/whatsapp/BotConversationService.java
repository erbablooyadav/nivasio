package in.nivasio.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * WhatsApp bot conversation handler — processes incoming WA messages.
 * Uses Redis for message deduplication (24hr TTL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotConversationService {

    private final RedisTemplate<String, String> redis;

    @Value("${app.whatsapp.mock-mode:true}")
    private boolean mockMode;

    private static final String DEDUP_PREFIX = "wa_dedup:";

    public void processIncomingMessage(String payload) {
        // Extract messageId from payload for dedup
        String messageId = extractField(payload, "id");
        if (messageId == null)
            return;

        // Redis dedup: check if already processed
        String dedupKey = DEDUP_PREFIX + messageId;
        Boolean isNew = redis.opsForValue().setIfAbsent(dedupKey, "processed", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate message ignored: {}", messageId);
            return;
        }

        String from = extractField(payload, "from");
        String body = extractNestedField(payload, "text", "body");

        if (from == null || body == null) {
            log.debug("Incomplete message ignored");
            return;
        }

        log.info("[WA BOT] From: {}, Message: {}", maskPhone(from), body);

        if (mockMode) {
            log.info("[MOCK REPLY] To: {}, Reply: Namaste! Welcome to Nivasio. How can we help?", maskPhone(from));
        }
    }

    /**
     * Send a WhatsApp message (mock logs to console).
     */
    public void sendMessage(String to, String message) {
        if (mockMode) {
            log.info("[MOCK WA SEND] To: {}, Message: {}", maskPhone(to), message);
        }
        // Production: call Meta Cloud API
    }

    // Simple JSON field extractor (production: use Jackson)
    private String extractField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int idx = json.indexOf(search);
        if (idx == -1)
            return null;
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : null;
    }

    private String extractNestedField(String json, String parent, String field) {
        int parentIdx = json.indexOf("\"" + parent + "\"");
        if (parentIdx == -1)
            return null;
        String sub = json.substring(parentIdx);
        return extractField(sub, field);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
