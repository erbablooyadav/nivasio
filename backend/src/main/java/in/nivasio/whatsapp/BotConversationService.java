package in.nivasio.whatsapp;

import in.nivasio.model.Resident;
import in.nivasio.model.Ticket;
import in.nivasio.repository.ResidentRepository;
import in.nivasio.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * WhatsApp bot conversation handler — processes incoming WA messages.
 * Features: Multi-language (EN/HI), interactive menus, ticket status lookup,
 * Redis deduplication (24hr TTL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotConversationService {

    private final RedisTemplate<String, String> redis;
    private final ResidentRepository residentRepo;
    private final TicketRepository ticketRepo;

    @Value("${app.whatsapp.mock-mode:true}")
    private boolean mockMode;

    private static final String DEDUP_PREFIX = "wa_dedup:";
    private static final String LANG_PREFIX = "wa_lang:";

    public void processIncomingMessage(String payload) {
        String messageId = extractField(payload, "id");
        if (messageId == null)
            return;

        // Redis dedup
        String dedupKey = DEDUP_PREFIX + messageId;
        Boolean isNew = redis.opsForValue().setIfAbsent(dedupKey, "processed", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate message ignored: {}", messageId);
            return;
        }

        String from = extractField(payload, "from");
        String body = extractNestedField(payload, "text", "body");
        if (from == null || body == null)
            return;

        log.info("[WA BOT] From: {}, Message: {}", maskPhone(from), body);
        String lang = getLanguagePreference(from);
        String response = handleMessage(from, body.trim().toLowerCase(), lang);
        sendMessage(from, response);
    }

    private String handleMessage(String from, String input, String lang) {
        // Language switch
        if ("hi".equals(input) || "hindi".equals(input)) {
            setLanguagePreference(from, "hi");
            return "🏠 *निवासियो में आपका स्वागत है!*\n\n"
                    + "कृपया एक विकल्प चुनें:\n"
                    + "1️⃣ शिकायत दर्ज करें\n"
                    + "2️⃣ शिकायत की स्थिति देखें\n"
                    + "3️⃣ किराया स्थिति\n"
                    + "4️⃣ मदद\n"
                    + "0️⃣ भाषा बदलें";
        }
        if ("en".equals(input) || "english".equals(input)) {
            setLanguagePreference(from, "en");
            return getMainMenu("en");
        }

        // Main menu options
        switch (input) {
            case "0":
                return "en".equals(lang)
                        ? "🌐 *Language / भाषा*\nType 'hi' for हिंदी\nType 'en' for English"
                        : "🌐 *भाषा बदलें*\n'hi' टाइप करें हिंदी के लिए\n'en' टाइप करें English के लिए";
            case "1":
                return "en".equals(lang)
                        ? "📝 *Raise Complaint*\nPlease describe your issue:\n\n_Example: AC not working in room 201_"
                        : "📝 *शिकायत दर्ज करें*\nकृपया अपनी समस्या बताएं:\n\n_उदाहरण: कमरा 201 में AC काम नहीं कर रहा_";
            case "2":
                return getTicketStatus(from, lang);
            case "3":
                return getRentStatus(from, lang);
            case "4":
            case "help":
            case "menu":
                return getMainMenu(lang);
            default:
                // If it looks like a complaint, acknowledge it
                if (input.length() > 10) {
                    return "en".equals(lang)
                            ? "✅ *Complaint Received!*\nOur team will review and assign it shortly.\nYou'll receive updates here.\n\nType 'menu' to go back."
                            : "✅ *शिकायत प्राप्त हुई!*\nहमारी टीम इसे जल्द ही देखेगी।\nआपको यहाँ अपडेट मिलेगा।\n\n'menu' टाइप करें वापस जाने के लिए।";
                }
                return getMainMenu(lang);
        }
    }

    private String getMainMenu(String lang) {
        if ("hi".equals(lang)) {
            return "🏠 *निवासियो*\n\n"
                    + "कृपया एक विकल्प चुनें:\n"
                    + "1️⃣ शिकायत दर्ज करें\n"
                    + "2️⃣ शिकायत की स्थिति\n"
                    + "3️⃣ किराया स्थिति\n"
                    + "4️⃣ मदद\n"
                    + "0️⃣ भाषा बदलें";
        }
        return "🏠 *Welcome to Nivasio!*\n\n"
                + "Please choose an option:\n"
                + "1️⃣ Raise a Complaint\n"
                + "2️⃣ Check Complaint Status\n"
                + "3️⃣ Rent Status\n"
                + "4️⃣ Help\n"
                + "0️⃣ Change Language";
    }

    private String getTicketStatus(String from, String lang) {
        Optional<Resident> resident = residentRepo.findByPhone(from);
        if (resident.isEmpty()) {
            return "en".equals(lang)
                    ? "❌ Phone not registered. Please contact your PG admin."
                    : "❌ यह नंबर पंजीकृत नहीं है। कृपया अपने PG admin से संपर्क करें।";
        }
        String tenantId = resident.get().getTenantId();
        List<Ticket> tickets = ticketRepo.findByTenantIdAndStatusIn(tenantId,
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS"));

        // Filter by resident's room
        String roomNo = resident.get().getRoomNo();
        List<Ticket> myTickets = tickets.stream()
                .filter(t -> roomNo != null && roomNo.equals(t.getRoomNo()))
                .limit(5)
                .toList();

        if (myTickets.isEmpty()) {
            return "en".equals(lang)
                    ? "✅ No active complaints for your room."
                    : "✅ आपके कमरे के लिए कोई सक्रिय शिकायत नहीं।";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("en".equals(lang) ? "📋 *Your Active Complaints:*\n\n" : "📋 *आपकी सक्रिय शिकायतें:*\n\n");
        for (Ticket t : myTickets) {
            sb.append("• ").append(t.getType())
                    .append(" — ").append(t.getStatus())
                    .append(t.getAssignedToName() != null ? " (by " + t.getAssignedToName() + ")" : "")
                    .append("\n");
        }
        return sb.toString();
    }

    private String getRentStatus(String from, String lang) {
        return "en".equals(lang)
                ? "💰 *Rent Status*\nPlease check with your PG admin for rent details.\n\nType 'menu' for main menu."
                : "💰 *किराया स्थिति*\nकृपया किराये की जानकारी के लिए अपने PG admin से संपर्क करें।\n\n'menu' टाइप करें मेन मेनू के लिए।";
    }

    private String getLanguagePreference(String phone) {
        String lang = redis.opsForValue().get(LANG_PREFIX + phone);
        if (lang != null)
            return lang;
        // Check DB resident preference
        Optional<Resident> r = residentRepo.findByPhone(phone);
        return r.map(res -> res.getLanguagePreference() != null ? res.getLanguagePreference() : "en").orElse("en");
    }

    private void setLanguagePreference(String phone, String lang) {
        redis.opsForValue().set(LANG_PREFIX + phone, lang, Duration.ofDays(90));
    }

    /** Send a WhatsApp message (mock logs to console). */
    public void sendMessage(String to, String message) {
        if (mockMode) {
            log.info("[MOCK WA SEND] To: {}, Message: {}", maskPhone(to), message);
        }
        // Production: call Meta Cloud API with template/text message
    }

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
        return extractField(json.substring(parentIdx), field);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
