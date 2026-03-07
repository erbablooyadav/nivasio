package com.pgmanager.service;

import com.pgmanager.dto.TicketRequest;
import com.pgmanager.exception.BadRequestException;
import com.pgmanager.exception.ResourceNotFoundException;
import com.pgmanager.model.AuditLog;
import com.pgmanager.model.AppUser;
import com.pgmanager.model.Ticket;
import com.pgmanager.repository.AuditLogRepository;
import com.pgmanager.repository.TicketRepository;
import com.pgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.sla.default-threshold-minutes}")
    private int defaultSlaThresholdMinutes;

    private static final AtomicInteger ticketCounter = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMM");

    public Ticket createTicket(String tenantId, TicketRequest request, String raisedByUserId) {
        String prefix = getTicketPrefix(request.getType());
        String datePart = LocalDate.now().format(DATE_FMT);
        int seq = ticketCounter.getAndIncrement();
        String ticketId = String.format("%s-%s-%04d", prefix, datePart, seq);

        String department = mapTypeToDepartment(request.getType());

        Ticket ticket = Ticket.builder()
                .tenantId(tenantId)
                .ticketId(ticketId)
                .type(request.getType().toUpperCase())
                .department(department)
                .status(Ticket.Status.OPEN)
                .priority(request.getPriority() != null ? Ticket.Priority.valueOf(request.getPriority().toUpperCase())
                        : Ticket.Priority.NORMAL)
                .roomNo(request.getRoomNo())
                .description(request.getDescription())
                .raisedByUserId(raisedByUserId)
                .slaThresholdMinutes(defaultSlaThresholdMinutes)
                .slaBreach(false)
                .slaDeadline(Instant.now().plusSeconds(defaultSlaThresholdMinutes * 60L))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Auto-assign to available staff in department
        autoAssignStaff(tenantId, department, ticket);

        Ticket saved = ticketRepository.save(ticket);

        // Audit log
        createAuditLog(tenantId, ticketId, raisedByUserId, "CREATED", null, "OPEN");

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/tickets/" + tenantId, saved);

        log.info("Ticket created: {} (tenant: {}, room: {})", ticketId, tenantId, request.getRoomNo());
        return saved;
    }

    public Ticket updateStatus(String tenantId, String ticketId, String newStatus, String updatedBy) {
        Ticket ticket = ticketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        String oldStatus = ticket.getStatus().name();
        Ticket.Status status = Ticket.Status.valueOf(newStatus.toUpperCase());

        ticket.setStatus(status);
        ticket.setUpdatedAt(Instant.now());

        if (status == Ticket.Status.DONE) {
            ticket.setResolvedAt(Instant.now());
        } else if (status == Ticket.Status.CLOSED) {
            ticket.setClosedAt(Instant.now());
        }

        Ticket updated = ticketRepository.save(ticket);

        // Audit log
        createAuditLog(tenantId, ticketId, updatedBy, "STATUS_CHANGE", oldStatus, newStatus);

        // Broadcast update
        messagingTemplate.convertAndSend("/topic/tickets/" + tenantId, updated);

        log.info("Ticket {} status updated: {} → {}", ticketId, oldStatus, newStatus);
        return updated;
    }

    public Ticket assignTicket(String tenantId, String ticketId, String staffId, String assignedBy) {
        Ticket ticket = ticketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        AppUser staff = userRepository.findByTenantIdAndId(tenantId, staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        ticket.setAssignedTo(staffId);
        ticket.setAssignedToName(staff.getName());
        ticket.setUpdatedAt(Instant.now());

        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
        }

        Ticket updated = ticketRepository.save(ticket);

        createAuditLog(tenantId, ticketId, assignedBy, "ASSIGNMENT", null, staff.getName());
        messagingTemplate.convertAndSend("/topic/tickets/" + tenantId, updated);

        return updated;
    }

    public Page<Ticket> getTickets(String tenantId, String status, String department,
            String roomNo, String assignedTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isEmpty()) {
            return ticketRepository.findByTenantIdAndStatus(
                    tenantId, Ticket.Status.valueOf(status.toUpperCase()), pageable);
        }
        if (department != null && !department.isEmpty()) {
            return ticketRepository.findByTenantIdAndDepartment(tenantId, department, pageable);
        }
        if (roomNo != null && !roomNo.isEmpty()) {
            return ticketRepository.findByTenantIdAndRoomNo(tenantId, roomNo, pageable);
        }
        if (assignedTo != null && !assignedTo.isEmpty()) {
            return ticketRepository.findByTenantIdAndAssignedTo(tenantId, assignedTo, pageable);
        }
        return ticketRepository.findByTenantId(tenantId, pageable);
    }

    public Ticket getTicketById(String tenantId, String ticketId) {
        return ticketRepository.findByTenantIdAndTicketId(tenantId, ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    public void checkSlaBreaches() {
        List<Ticket> breached = ticketRepository.findByStatusInAndSlaBreachFalseAndSlaDeadlineBefore(
                List.of(Ticket.Status.OPEN, Ticket.Status.IN_PROGRESS), Instant.now());

        for (Ticket ticket : breached) {
            ticket.setSlaBreach(true);
            ticket.setUpdatedAt(Instant.now());
            ticketRepository.save(ticket);

            messagingTemplate.convertAndSend("/topic/tickets/" + ticket.getTenantId(), ticket);
            log.warn("SLA breach for ticket: {} (tenant: {})", ticket.getTicketId(), ticket.getTenantId());
        }
    }

    // --- Private helpers ---

    private void autoAssignStaff(String tenantId, String department, Ticket ticket) {
        List<AppUser> staffList = userRepository.findByTenantIdAndDepartment(tenantId, department);
        if (!staffList.isEmpty()) {
            // Simple round-robin: assign to first available staff
            AppUser staff = staffList.get(0);
            ticket.setAssignedTo(staff.getId());
            ticket.setAssignedToName(staff.getName());
        }
    }

    private String getTicketPrefix(String type) {
        return switch (type.toUpperCase()) {
            case "HOUSEKEEPING" -> "HK";
            case "LAUNDRY" -> "LDY";
            case "MAINTENANCE" -> "MNT";
            case "FOOD" -> "FD";
            default -> "GEN";
        };
    }

    private String mapTypeToDepartment(String type) {
        return switch (type.toUpperCase()) {
            case "HOUSEKEEPING" -> "HOUSEKEEPING";
            case "LAUNDRY" -> "LAUNDRY";
            case "MAINTENANCE" -> "MAINTENANCE";
            case "FOOD" -> "FOOD";
            default -> "GENERAL";
        };
    }

    private void createAuditLog(String tenantId, String ticketId, String changedBy,
            String action, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .tenantId(tenantId)
                .ticketId(ticketId)
                .changedBy(changedBy)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}
