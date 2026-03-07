package com.pgmanager.controller;

import com.pgmanager.dto.ApiResponse;
import com.pgmanager.model.FoodFeedback;
import com.pgmanager.security.UserPrincipal;
import com.pgmanager.service.FoodFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/food-feedback")
@RequiredArgsConstructor
public class FoodFeedbackController {

    private final FoodFeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FoodFeedback>>> getFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FoodFeedback> feedback = feedbackService.getFeedback(principal.getTenantId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(feedback));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        feedbackService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(ApiResponse.ok(null, "Status updated"));
    }
}
