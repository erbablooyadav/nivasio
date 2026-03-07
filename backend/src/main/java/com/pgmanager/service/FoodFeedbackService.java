package com.pgmanager.service;

import com.pgmanager.model.FoodFeedback;
import com.pgmanager.repository.FoodFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodFeedbackService {

    private final FoodFeedbackRepository feedbackRepository;

    public FoodFeedback createFeedback(String tenantId, String userId, String userName,
            String roomNo, String category, String message) {
        FoodFeedback feedback = FoodFeedback.builder()
                .tenantId(tenantId)
                .userId(userId)
                .userName(userName)
                .roomNo(roomNo)
                .category(FoodFeedback.Category.valueOf(category.toUpperCase()))
                .message(message)
                .status("OPEN")
                .createdAt(Instant.now())
                .build();

        FoodFeedback saved = feedbackRepository.save(feedback);
        log.info("Food feedback created for tenant {} from room {}", tenantId, roomNo);
        return saved;
    }

    public Page<FoodFeedback> getFeedback(String tenantId, int page, int size) {
        return feedbackRepository.findByTenantId(tenantId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public void updateStatus(String id, String status) {
        FoodFeedback feedback = feedbackRepository.findById(id).orElseThrow();
        feedback.setStatus(status);
        if ("RESOLVED".equals(status)) {
            feedback.setResolvedAt(Instant.now());
        }
        feedbackRepository.save(feedback);
    }
}
