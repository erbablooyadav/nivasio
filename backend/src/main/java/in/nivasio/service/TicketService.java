package in.nivasio.service;

import in.nivasio.dto.TicketRequest;
import in.nivasio.exception.*;
import in.nivasio.model.Staff;
import in.nivasio.model.Ticket;
import in.nivasio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final SimpMessagingTemplate ws;
    private final AuditService auditService;
    private final RedisTemplate<String, String> redis;

    private static final String ROUND_ROBIN_PREFIX = "rr:";

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
                .propertyId(request.getPropertyId())
                .ticketId(ticketId)
                .type(request.getType())
                .department(dept)
                .status("OPEN")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .roomNo(request.getRoomNo())
                .description(request.getDescription())
                .photos(request.getPhotos())
                .slaDeadline(Instant.now().plusSeconds(7200))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ticket = ticketRepo.save(ticket);

        // Round-robin auto-assign
        autoAssignRoundRobin(ticket);

        // Flag if recurring
        boolean isRecurring = checkRecurring(tenantId, request.getRoomNo(), request.getType());
        if (isRecurring) {
            ticket.setRecurringFlag(true);
            ticket.setPriority("URGENT");
            ticketRepo.save(ticket);
        }

        auditService.log(tenantId, "TICKET", ticketId, "CREATED", creatorId, null);
        ws.convertAndSend("/topic/tickets/" + tenantId, ticket);
        return ticket;
    }

    public Ticket updateStatus(String tenantId, String ticketId, String newStatus, String userId) {
        Ticket ticket = ticketRepo.findByTicketIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        validateStatusTransition(ticket.getStatus(), newStatus);

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
        auditService.log(tenantId, "TICKET", ticketId, "STATUS_CHANGED", userId, oldStatus + " → " + newStatus);
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
        if (status != null && department != null)
            return ticketRepo.findByTenantIdAndStatusAndDepartment(tenantId, status, department, pageable);
        if (status != null)
            return ticketRepo.findByTenantIdAndStatus(tenantId, status, pageable);
        if (department != null)
            return ticketRepo.findByTenantIdAndDepartment(tenantId, department, pageable);
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

    /** Round-robin auto-assign via Redis counter for fair distribution. */
    private void autoAssignRoundRobin(Ticket ticket) {
        List<Staff> available = staffRepo.findByTenantIdAndDepartmentAndActiveTrue(
                ticket.getTenantId(), ticket.getDepartment());
        if (available.isEmpty())
            return;

        String rrKey = ROUND_ROBIN_PREFIX + ticket.getTenantId() + ":" + ticket.getDepartment();
        Long idx = redis.opsForValue().increment(rrKey);
        if (idx == null)
            idx = 0L;
        Staff staff = available.get((int) (idx % available.size()));

        ticket.setAssignedTo(staff.getId());
        ticket.setAssignedToName(staff.getName());
        ticket.setStatus("ASSIGNED");
        ticket.setAssignedAt(Instant.now());
        ticketRepo.save(ticket);
        log.info("Auto-assigned ticket {} to {} (round-robin)", ticket.getTicketId(), staff.getName());
    }

    private boolean checkRecurring(String tenantId, String roomNo, String type) {
        Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 3600);
        List<Ticket> recent = ticketRepo.findByTenantIdAndRoomNoAndTypeAndCreatedAtAfter(
                tenantId, roomNo, type, sevenDaysAgo);
        if (recent.size() >= 3) {
            log.warn("RECURRING: room {} type {} → {} tickets in 7 days", roomNo, type, recent.size());
            return true;
        }
        return false;
    }

    /** Security: enforce valid status transitions. */
    private void validateStatusTransition(String from, String to) {
        Map<String, List<String>> allowed = Map.of(
                "OPEN", List.of("ASSIGNED", "IN_PROGRESS", "CLOSED"),
                "ASSIGNED", List.of("IN_PROGRESS", "CLOSED"),
                "IN_PROGRESS", List.of("DONE", "CLOSED"),
                "DONE", List.of("CLOSED", "REOPENED"),
                "REOPENED", List.of("ASSIGNED", "IN_PROGRESS", "CLOSED"),
                "CLOSED", List.of());
        if (!allowed.getOrDefault(from, List.of()).contains(to)) {
            throw new BadRequestException("Invalid transition: " + from + " → " + to);
        }
    }
}
