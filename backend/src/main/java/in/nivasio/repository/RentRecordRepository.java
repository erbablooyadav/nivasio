package in.nivasio.repository;

import in.nivasio.model.RentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RentRecordRepository extends MongoRepository<RentRecord, String> {
    Page<RentRecord> findByTenantId(String tenantId, Pageable pageable);

    Page<RentRecord> findByTenantIdAndStatus(String tenantId, String status, Pageable pageable);

    Page<RentRecord> findByTenantIdAndMonth(String tenantId, String month, Pageable pageable);

    Optional<RentRecord> findByIdAndTenantId(String id, String tenantId);

    List<RentRecord> findByTenantIdAndResidentIdAndMonth(String tenantId, String residentId, String month);

    List<RentRecord> findByStatusAndDueDateBefore(String status, Instant now);

    List<RentRecord> findByStatusAndReminderSentFalseAndDueDateBetween(String status, Instant start, Instant end);

    long countByTenantIdAndStatus(String tenantId, String status);

    long countByTenantIdAndMonth(String tenantId, String month);
}
