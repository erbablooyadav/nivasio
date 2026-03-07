package com.pgmanager.repository;

import com.pgmanager.model.FoodFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FoodFeedbackRepository extends MongoRepository<FoodFeedback, String> {
    Page<FoodFeedback> findByTenantId(String tenantId, Pageable pageable);

    List<FoodFeedback> findByTenantIdAndStatus(String tenantId, String status);

    long countByTenantId(String tenantId);
}
