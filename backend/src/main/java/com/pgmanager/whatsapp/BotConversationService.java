package com.pgmanager.whatsapp;

import com.pgmanager.dto.TicketRequest;
import com.pgmanager.model.AppUser;
import com.pgmanager.model.Ticket;
import com.pgmanager.repository.UserRepository;
import com.pgmanager.service.FoodFeedbackService;
import com.pgmanager.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles WhatsApp bot conversation logic:
 * Greeting → Service Selection → Room Confirmation → Ticket Creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotConversationService {

    private final WhatsAppService whatsAppService;
    private final TicketService ticketService;
    private final FoodFeedbackService foodFeedbackService;
    private final UserRepository userRepository;

    private static final Pattern ROOM_PATTERN = Pattern.compile("\\b(\\d{1,4}[A-Za-z]?)\\b");

    public void processMessage(String from, String messageBody, String messageId) {
        log.info("Processing message from {}: {}", from, messageBody);

        // Find user by phone
        Optional<AppUser> userOpt = userRepository.findByPhone(from);
        if (userOpt.isEmpty()) {
            whatsAppService.sendTextMessage(from,
                    "⚠️ Your number is not registered. Please contact your PG admin to register.");
            return;
        }

        AppUser user = userOpt.get();
        String tenantId = user.getTenantId();

        WhatsAppService.ConversationState state = whatsAppService.getConversation(from);
        if (state == null) {
            state = WhatsAppService.ConversationState.builder()
                    .step(WhatsAppService.BotStep.GREETING)
                    .tenantId(tenantId)
                    .userId(user.getId())
                    .build();
        }

        String text = messageBody.trim().toLowerCase();

        switch (state.getStep()) {
            case GREETING -> handleGreeting(from, state);

            case SERVICE_SELECTION -> handleServiceSelection(from, text, state);

            case ROOM_CONFIRMATION -> handleRoomConfirmation(from, messageBody, state);

            default -> handleGreeting(from, state);
        }
    }

    private void handleGreeting(String from, WhatsAppService.ConversationState state) {
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("1", "🧹 Housekeeping");
        buttons.put("2", "👕 Laundry");
        buttons.put("3", "🔧 Maintenance");
        buttons.put("4", "🍽️ Food Feedback");
        buttons.put("5", "📝 General Complaint");

        whatsAppService.sendInteractiveButtons(from,
                "Welcome to PG Manager! 🏠\nHow can we help you today?\n\n" +
                        "1️⃣ Housekeeping\n2️⃣ Laundry\n3️⃣ Maintenance\n4️⃣ Food Feedback\n5️⃣ General Complaint",
                buttons);

        state.setStep(WhatsAppService.BotStep.SERVICE_SELECTION);
        whatsAppService.setConversation(from, state);
    }

    private void handleServiceSelection(String from, String text, WhatsAppService.ConversationState state) {
        String service = detectService(text);
        if (service == null) {
            whatsAppService.sendTextMessage(from,
                    "Please select a valid option:\n1️⃣ Housekeeping\n2️⃣ Laundry\n3️⃣ Maintenance\n4️⃣ Food Feedback\n5️⃣ General Complaint");
            return;
        }

        state.setSelectedService(service);
        state.setStep(WhatsAppService.BotStep.ROOM_CONFIRMATION);
        whatsAppService.setConversation(from, state);

        whatsAppService.sendTextMessage(from, "Please confirm your room number:");
    }

    private void handleRoomConfirmation(String from, String text, WhatsAppService.ConversationState state) {
        String roomNo = extractRoomNumber(text);
        if (roomNo == null) {
            whatsAppService.sendTextMessage(from, "Please enter a valid room number (e.g., 402, 301A):");
            return;
        }

        state.setRoomNo(roomNo);

        // Handle food feedback separately
        if ("FOOD".equals(state.getSelectedService())) {
            foodFeedbackService.createFeedback(
                    state.getTenantId(), state.getUserId(), "Tenant", roomNo,
                    "FEEDBACK", "Food feedback from room " + roomNo);
            whatsAppService.sendTextMessage(from,
                    "✅ Food feedback submitted from Room " + roomNo + "! Thank you.");
        } else {
            // Create ticket
            TicketRequest request = new TicketRequest();
            request.setType(state.getSelectedService());
            request.setRoomNo(roomNo);
            request.setDescription(state.getSelectedService() + " request from room " + roomNo);

            Ticket ticket = ticketService.createTicket(state.getTenantId(), request, state.getUserId());
            whatsAppService.sendTicketConfirmation(from, ticket.getTicketId(), state.getSelectedService());
        }

        whatsAppService.clearConversation(from);
    }

    private String detectService(String text) {
        if (text.contains("1") || text.contains("housekeeping") || text.contains("cleaning")
                || text.contains("clean")) {
            return "HOUSEKEEPING";
        }
        if (text.contains("2") || text.contains("laundry") || text.contains("wash") || text.contains("clothes")) {
            return "LAUNDRY";
        }
        if (text.contains("3") || text.contains("maintenance") || text.contains("repair") || text.contains("fix")
                || text.contains("broken")) {
            return "MAINTENANCE";
        }
        if (text.contains("4") || text.contains("food") || text.contains("meal") || text.contains("kitchen")) {
            return "FOOD";
        }
        if (text.contains("5") || text.contains("complaint") || text.contains("general") || text.contains("other")) {
            return "GENERAL";
        }
        return null;
    }

    private String extractRoomNumber(String text) {
        Matcher matcher = ROOM_PATTERN.matcher(text.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
