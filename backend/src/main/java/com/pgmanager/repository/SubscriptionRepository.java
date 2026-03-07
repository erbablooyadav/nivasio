package com.pgmanager.repository;

import com.pgmanager.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByTenantId(String tenantId);
}
