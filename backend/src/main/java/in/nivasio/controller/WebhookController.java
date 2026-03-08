package in.nivasio.controller;

import in.nivasio.security.WebhookSignatureVerifier;
import in.nivasio.whatsapp.BotConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final BotConversationService botService;
    private final WebhookSignatureVerifier signatureVerifier;

    @Value("${app.whatsapp.verify-token:nivasio-verify}")
    private String verifyToken;

    @Value("${app.whatsapp.app-secret:}")
    private String appSecret;

    // Meta webhook verification
    @GetMapping
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Forbidden");
    }

    // Meta webhook message receive — always return 200 fast, process async
    @PostMapping
    public ResponseEntity<String> receive(@RequestBody String payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        // Verify HMAC signature (skip in mock mode)
        if (appSecret != null && !appSecret.isEmpty() && signature != null) {
            if (!signatureVerifier.verifyMetaSignature(payload, signature, appSecret)) {
                log.warn("WEBHOOK: Invalid HMAC signature rejected");
                return ResponseEntity.status(403).body("Invalid signature");
            }
        }

        // Process asynchronously — return 200 within 1 second
        processAsync(payload);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    @Async("webhookExecutor")
    public void processAsync(String payload) {
        try {
            // Simple JSON extraction (production: use Jackson ObjectMapper)
            botService.processIncomingMessage(payload);
        } catch (Exception e) {
            log.error("Webhook processing error: ", e);
        }
    }
}
