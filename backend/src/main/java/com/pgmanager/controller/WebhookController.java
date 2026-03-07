package com.pgmanager.controller;

import com.pgmanager.whatsapp.BotConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final BotConversationService botService;

    @Value("${app.whatsapp.verify-token}")
    private String verifyToken;

    /**
     * Meta webhook verification (GET)
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Meta webhook message handler (POST)
     * Must respond within 5 seconds — processing is async
     */
    @PostMapping
    public ResponseEntity<String> handleMessage(@RequestBody Map<String, Object> payload) {
        // Respond immediately
        log.debug("Webhook received: {}", payload);

        // Process async
        try {
            processWebhookAsync(payload);
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    @SuppressWarnings("unchecked")
    private void processWebhookAsync(Map<String, Object> payload) {
        try {
            var entry = (java.util.List<Map<String, Object>>) payload.get("entry");
            if (entry == null || entry.isEmpty())
                return;

            for (Map<String, Object> e : entry) {
                var changes = (java.util.List<Map<String, Object>>) e.get("changes");
                if (changes == null)
                    continue;

                for (Map<String, Object> change : changes) {
                    var value = (Map<String, Object>) change.get("value");
                    if (value == null)
                        continue;

                    var messages = (java.util.List<Map<String, Object>>) value.get("messages");
                    if (messages == null)
                        continue;

                    for (Map<String, Object> message : messages) {
                        String from = (String) message.get("from");
                        String msgId = (String) message.get("id");
                        String type = (String) message.get("type");

                        String body = "";
                        if ("text".equals(type)) {
                            var textObj = (Map<String, Object>) message.get("text");
                            body = textObj != null ? (String) textObj.get("body") : "";
                        } else if ("interactive".equals(type)) {
                            var interactive = (Map<String, Object>) message.get("interactive");
                            if (interactive != null) {
                                var buttonReply = (Map<String, Object>) interactive.get("button_reply");
                                if (buttonReply != null) {
                                    body = (String) buttonReply.get("title");
                                }
                            }
                        }

                        if (!body.isEmpty()) {
                            botService.processMessage(from, body, msgId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing webhook payload: ", e);
        }
    }
}
