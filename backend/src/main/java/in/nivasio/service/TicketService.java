package in.nivasio.service;

import in.nivasio.dto.TicketRequest;
import in.nivasio.exception.*;
import in.nivasio.model.Staff;
import in.nivasio.model.Ticket;
import in.nivasio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepo;
    private final StaffRepository staffRepo;
    private final AuditLogRepository auditRepo;
    private final SimpMessagingTemplate ws;
    private final AuditService auditService;

    private static final Map<String, String> TYPE_TO_DEPT = Map.of(
            "HOUSEKEEPING", "HOUSEKEEPING",
            "LAUNDRY", "LAUNDRY",
            "MAINTENANCE", "MAINTENANCE",
            "FOOD", "FOOD",
            "GENERAL", "GENERAL");

    private static final Map<String, String> TYPE_PREFIX = Map.of(
            "HOUSEKEEPING", "HK",
            "LAUNDRY", "LDY",
            "MAINTENANCE", "MNT",
            "FOOD", "FOOD",
            "GENERAL", "GEN");

    private final AtomicInteger counter = new AtomicInteger(1);

    public Ticket createTicket(String tenantId, TicketRequest request, String creatorId) {
        String dept = TYPE_TO_DEPT.getOrDefault(request.getType(), "GENERAL");
        String prefix = TYPE_PREFIX.getOrDefault(request.getType(), "GEN");
        String ticketId = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMM"))
                + "-" + String.format("%04d", counter.getAndIncrement());

        Ticket ticket = Ticket.builder()
                .tenantId(tenantId)
                .ticketId(ticketId)
                .type(request.getType())
                .department(dept)
                .status("OPEN")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .roomNo(request.getRoomNo())
                .description(request.getDescription())
                .slaDeadline(Instant.now().plusSeconds(7200))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ticket = ticketRepo.save(ticket);

        // Auto-assign to available staff
        autoAssign(ticket);

        // Check recurring
        checkRecurring(tenantId, request.getRoomNo(), request.getType());

        // Audit
        auditService.log(tenantId, "TICKET", ticketId, "CREATED", creatorId, null);

        // WebSocket broadcast
        ws.convertAndSend("/topic/tickets/" + tenantId, ticket);

        return ticket;
    }

    public Ticket updateStatus(String tenantId, String ticketId, String newStatus, String userId) {
        Ticket ticket = ticketRepo.findByTicketIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        String oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());

        if ("ASSIGNED".equals(newStatus))
            ticket.setAssignedAt(Instant.now());
        if ("IN_PROGRESS".equals(newStatus))
            ticket.setStartedAt(Instant.now());
        if ("DONE".equals(newStatus))
            ticket.setCompletedAt(Instant.now());
        if ("CLOSED".equals(newStatus))
            ticket.setClosedAt(Instant.now());

        ticket = ticketRepo.save(ticket);

        auditService.log(tenantId, "TICKET", ticketId, "STATUS_CHANGED", userId,
                oldStatus + " → " + newStatus);

        ws.convertAndSend("/topic/tickets/" + tenantId, ticket);
        return ticket;
    }

    public Ticket assignTicket(String tenantId, String ticketId, String staffId, String userId) {
        Ticket ticket = ticketRepo.findByTicketIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        Staff staff = staffRepo.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + staffId));

        ticket.setAssignedTo(staffId);
        ticket.setAssignedToName(staff.getName());
        ticket.setStatus("ASSIGNED");
        ticket.setAssignedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());

        ticket = ticketRepo.save(ticket);

        auditService.log(tenantId, "TICKET", ticketId, "ASSIGNED", userId, "to " + staff.getName());
        ws.convertAndSend("/topic/tickets/" + tenantId, ticket);
        return ticket;
    }

    public Page<Ticket> getTickets(String tenantId, String status, String department,
            String roomNo, String assignedTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && department != null) {
            return ticketRepo.findByTenantIdAndStatusAndDepartment(tenantId, status, department, pageable);
        } else if (status != null) {
            return ticketRepo.findByTenantIdAndStatus(tenantId, status, pageable);
        } else if (department != null) {
            return ticketRepo.findByTenantIdAndDepartment(tenantId, department, pageable);
        }
        return ticketRepo.findByTenantId(tenantId, pageable);
    }

    public Ticket getTicketById(String tenantId, String ticketId) {
        return ticketRepo.findByTicketIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    public void checkSlaBreaches() {
        List<Ticket> breached = ticketRepo.findByStatusInAndSlaBreachFalseAndSlaDeadlineBefore(
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS"), Instant.now());
        for (Ticket t : breached) {
            t.setSlaBreach(true);
            t.setEscalatedAt(Instant.now());
            ticketRepo.save(t);
            log.warn("SLA BREACH: ticket {} for tenant {}", t.getTicketId(), t.getTenantId());
            ws.convertAndSend("/topic/tickets/" + t.getTenantId(), t);
        }
    }

    private void autoAssign(Ticket ticket) {
        List<Staff> available = staffRepo.findByTenantIdAndDepartmentAndActiveTrue(
                ticket.getTenantId(), ticket.getDepartment());
        if (!available.isEmpty()) {
            Staff staff = available.get(0);
            ticket.setAssignedTo(staff.getId());
            ticket.setAssignedToName(staff.getName());
            ticket.setStatus("ASSIGNED");
            ticket.setAssignedAt(Instant.now());
            ticketRepo.save(ticket);
        }
    }

    private void checkRecurring(String tenantId, String roomNo, String type) {
        Instant thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 3600);
        List<Ticket> recent = ticketRepo.findByTenantIdAndRoomNoAndTypeAndCreatedAtAfter(
                tenantId, roomNo, type, thirtyDaysAgo);
        if (recent.size() >= 3) {
            log.warn("RECURRING ISSUE: room {} type {} has {} tickets in 30 days", roomNo, type, recent.size());
        }
    }
}
