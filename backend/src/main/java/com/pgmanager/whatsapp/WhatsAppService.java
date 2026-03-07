package com.pgmanager.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock WhatsApp service for development/testing.
 * In production, this sends actual messages via Meta Cloud API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    @Value("${app.whatsapp.mock-mode}")
    private boolean mockMode;

    @Value("${app.whatsapp.access-token}")
    private String accessToken;

    @Value("${app.whatsapp.phone-number-id}")
    private String phoneNumberId;

    // Conversation state tracking: phone -> state
    private final Map<String, ConversationState> conversations = new ConcurrentHashMap<>();

    public void sendTextMessage(String to, String message) {
        if (mockMode) {
            log.info("[MOCK WA] To: {}, Message: {}", maskPhone(to), message);
            return;
        }
        // In production: call Meta Cloud API
        // POST https://graph.facebook.com/v18.0/{phoneNumberId}/messages
        log.info("WhatsApp message sent to: {}", maskPhone(to));
    }

    public void sendInteractiveButtons(String to, String bodyText, Map<String, String> buttons) {
        if (mockMode) {
            log.info("[MOCK WA] Interactive to: {}, Body: {}, Buttons: {}", maskPhone(to), bodyText, buttons);
            return;
        }
        // In production: call Meta Cloud API with interactive message
        log.info("WhatsApp interactive sent to: {}", maskPhone(to));
    }

    public void sendTicketConfirmation(String to, String ticketId, String type) {
        String message = String.format("✅ Ticket %s raised! Service: %s. Staff will arrive shortly.", ticketId, type);
        sendTextMessage(to, message);
    }

    public void sendTicketCompletedNotification(String to, String ticketId) {
        String message = String.format("✅ Ticket %s has been completed! Thank you for your patience.", ticketId);
        sendTextMessage(to, message);
    }

    public void sendStaffAlert(String to, String ticketId, String roomNo, String type) {
        String message = String.format("🔔 New ticket assigned!\nTicket: %s\nRoom: %s\nType: %s\nPlease attend to it.",
                ticketId, roomNo, type);
        sendTextMessage(to, message);
    }

    public ConversationState getConversation(String phone) {
        return conversations.get(phone);
    }

    public void setConversation(String phone, ConversationState state) {
        conversations.put(phone, state);
    }

    public void clearConversation(String phone) {
        conversations.remove(phone);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    public enum BotStep {
        GREETING, SERVICE_SELECTION, ROOM_CONFIRMATION, COMPLETE
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ConversationState {
        private BotStep step;
        private String tenantId;
        private String selectedService;
        private String roomNo;
        private String userId;
    }
}
