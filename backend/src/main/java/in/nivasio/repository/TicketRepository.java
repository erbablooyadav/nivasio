package in.nivasio.repository;

import in.nivasio.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends MongoRepository<Ticket, String> {
        Page<Ticket> findByTenantId(String tenantId, Pageable pageable);

        Page<Ticket> findByTenantIdAndStatus(String tenantId, String status, Pageable pageable);

        Page<Ticket> findByTenantIdAndDepartment(String tenantId, String department, Pageable pageable);

        Page<Ticket> findByTenantIdAndStatusAndDepartment(String tenantId, String status, String department,
                        Pageable pageable);

        Optional<Ticket> findByTicketId(String ticketId);

        Optional<Ticket> findByTicketIdAndTenantId(String ticketId, String tenantId);

        long countByTenantId(String tenantId);

        long countByTenantIdAndStatus(String tenantId, String status);

        long countByTenantIdAndSlaBreach(String tenantId, boolean slaBreach);

        long countByTenantIdAndCreatedAtAfter(String tenantId, Instant after);

        List<Ticket> findByStatusInAndSlaBreachFalseAndSlaDeadlineBefore(List<String> statuses, Instant now);

        List<Ticket> findByTenantIdAndRoomNoAndTypeAndCreatedAtAfter(String tenantId, String roomNo, String type,
                        Instant after);

        List<Ticket> findByTenantIdAndStatusIn(String tenantId, List<String> statuses);

        List<Ticket> findByTenantIdAndCreatedAtAfter(String tenantId, Instant after);

        List<Ticket> findByTenantIdAndAssignedTo(String tenantId, String assignedTo);
}
