package com.pgmanager.repository;

import com.pgmanager.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    Page<AuditLog> findByTenantIdAndTicketId(String tenantId, String ticketId, Pageable pageable);

    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);
}
