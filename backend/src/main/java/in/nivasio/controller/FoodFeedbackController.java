package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.FoodFeedback;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.FoodFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/food-feedback")
@RequiredArgsConstructor
public class FoodFeedbackController {

    private final FoodFeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FoodFeedback>>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedback(user.getTenantId(), page, size)));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<String>> resolve(@PathVariable String id) {
        feedbackService.updateStatus(id, "RESOLVED");
        return ResponseEntity.ok(ApiResponse.ok("Feedback resolved"));
    }
}
