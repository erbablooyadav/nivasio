package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.Notification;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifService;

    @GetMapping
    public ApiResponse<Page<Notification>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(notifService.getNotifications(user.getTenantId(), user.getUserId(), page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserPrincipal user) {
        long count = notifService.getUnreadCount(user.getTenantId(), user.getUserId());
        return ApiResponse.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<String> markRead(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        notifService.markAsRead(user.getTenantId(), user.getUserId(), id);
        return ApiResponse.ok("Marked as read");
    }

    @PutMapping("/read-all")
    public ApiResponse<String> markAllRead(@AuthenticationPrincipal UserPrincipal user) {
        notifService.markAllAsRead(user.getTenantId(), user.getUserId());
        return ApiResponse.ok("All marked as read");
    }
}
