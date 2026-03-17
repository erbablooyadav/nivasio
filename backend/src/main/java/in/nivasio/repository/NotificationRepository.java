package in.nivasio.repository;

import in.nivasio.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByTenantIdAndUserId(String tenantId, String userId, Pageable pageable);

    List<Notification> findByTenantIdAndUserIdAndReadFalse(String tenantId, String userId);

    long countByTenantIdAndUserIdAndReadFalse(String tenantId, String userId);
}
