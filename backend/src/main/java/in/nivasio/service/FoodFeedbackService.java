package in.nivasio.service;

import in.nivasio.model.FoodFeedback;
import in.nivasio.repository.FoodFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FoodFeedbackService {

    private final FoodFeedbackRepository feedbackRepo;

    public FoodFeedback createFeedback(String tenantId, String residentId, String residentName,
            String roomNo, String category, String message, int rating) {
        FoodFeedback fb = FoodFeedback.builder()
                .tenantId(tenantId)
                .residentId(residentId)
                .residentName(residentName)
                .roomNo(roomNo)
                .category(category)
                .message(message)
                .rating(rating)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        return feedbackRepo.save(fb);
    }

    public Page<FoodFeedback> getFeedback(String tenantId, int page, int size) {
        return feedbackRepo.findByTenantId(tenantId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public void updateStatus(String id, String status) {
        FoodFeedback fb = feedbackRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        fb.setStatus(status);
        if ("RESOLVED".equals(status))
            fb.setResolvedAt(Instant.now());
        feedbackRepo.save(fb);
    }
}
