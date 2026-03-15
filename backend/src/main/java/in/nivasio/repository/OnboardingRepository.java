package in.nivasio.repository;

import in.nivasio.model.Onboarding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OnboardingRepository extends MongoRepository<Onboarding, String> {
    Page<Onboarding> findByTenantIdAndStatus(String tenantId, String status, Pageable pageable);

    Page<Onboarding> findByTenantId(String tenantId, Pageable pageable);

    Optional<Onboarding> findByIdAndTenantId(String id, String tenantId);
}
