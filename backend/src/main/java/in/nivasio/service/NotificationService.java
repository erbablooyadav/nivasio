package in.nivasio.service;

import in.nivasio.model.Notification;
import in.nivasio.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Notification service: creates in-app notifications and pushes
 * real-time updates via WebSocket. Never includes sensitive data in
 * notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final SimpMessagingTemplate ws;

    public Notification create(String tenantId, String userId, String type,
            String title, String message, String referenceId) {
        Notification notif = Notification.builder()
                .tenantId(tenantId)
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .read(false)
                .createdAt(Instant.now())
                .build();
        notif = notifRepo.save(notif);

        // Push via WebSocket to the specific user
        ws.convertAndSend("/topic/notifications/" + tenantId + "/" + userId, notif);
        return notif;
    }

    public Page<Notification> getNotifications(String tenantId, String userId, int page, int size) {
        return notifRepo.findByTenantIdAndUserId(tenantId, userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public long getUnreadCount(String tenantId, String userId) {
        return notifRepo.countByTenantIdAndUserIdAndReadFalse(tenantId, userId);
    }

    public void markAsRead(String tenantId, String userId, String notifId) {
        Notification notif = notifRepo.findById(notifId).orElse(null);
        if (notif != null && notif.getTenantId().equals(tenantId) && notif.getUserId().equals(userId)) {
            notif.setRead(true);
            notifRepo.save(notif);
        }
    }

    public void markAllAsRead(String tenantId, String userId) {
        notifRepo.findByTenantIdAndUserIdAndReadFalse(tenantId, userId)
                .forEach(n -> {
                    n.setRead(true);
                    notifRepo.save(n);
                });
    }
}
