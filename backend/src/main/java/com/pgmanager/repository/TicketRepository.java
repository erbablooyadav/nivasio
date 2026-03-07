package com.pgmanager.repository;

import com.pgmanager.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    Optional<Ticket> findByTenantIdAndId(String tenantId, String id);

    Optional<Ticket> findByTenantIdAndTicketId(String tenantId, String ticketId);

    Optional<Ticket> findByWaMessageId(String waMessageId);

    Page<Ticket> findByTenantId(String tenantId, Pageable pageable);

    Page<Ticket> findByTenantIdAndStatus(String tenantId, Ticket.Status status, Pageable pageable);

    Page<Ticket> findByTenantIdAndDepartment(String tenantId, String department, Pageable pageable);

    Page<Ticket> findByTenantIdAndAssignedTo(String tenantId, String assignedTo, Pageable pageable);

    Page<Ticket> findByTenantIdAndRoomNo(String tenantId, String roomNo, Pageable pageable);

    List<Ticket> findByTenantIdAndStatus(String tenantId, Ticket.Status status);

    List<Ticket> findByTenantIdAndStatusAndSlaBreach(String tenantId, Ticket.Status status, boolean slaBreach);

    long countByTenantId(String tenantId);

    long countByTenantIdAndStatus(String tenantId, Ticket.Status status);

    long countByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);

    long countByTenantIdAndStatusAndCreatedAtBetween(String tenantId, Ticket.Status status, Instant start, Instant end);

    long countByTenantIdAndDepartment(String tenantId, String department);

    List<Ticket> findByStatusInAndSlaBreachFalseAndSlaDeadlineBefore(List<Ticket.Status> statuses, Instant now);
}
