package in.nivasio.repository;

import in.nivasio.model.FoodFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FoodFeedbackRepository extends MongoRepository<FoodFeedback, String> {
    Page<FoodFeedback> findByTenantId(String tenantId, Pageable pageable);
}
