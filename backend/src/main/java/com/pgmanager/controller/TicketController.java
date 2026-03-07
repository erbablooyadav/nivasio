package com.pgmanager.controller;

import com.pgmanager.dto.ApiResponse;
import com.pgmanager.dto.TicketRequest;
import com.pgmanager.model.Ticket;
import com.pgmanager.security.UserPrincipal;
import com.pgmanager.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> createTicket(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TicketRequest request) {
        Ticket ticket = ticketService.createTicket(principal.getTenantId(), request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Ticket created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Ticket>>> getTickets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Staff can only see their own dept tickets
        String dept = department;
        String assigned = assignedTo;
        if ("STAFF".equals(principal.getRole())) {
            dept = principal.getDepartment();
        }

        Page<Ticket> tickets = ticketService.getTickets(
                principal.getTenantId(), status, dept, roomNo, assigned, page, size);
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<Ticket>> getTicket(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String ticketId) {
        Ticket ticket = ticketService.getTicketById(principal.getTenantId(), ticketId);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @PutMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        Ticket ticket = ticketService.updateStatus(
                principal.getTenantId(), ticketId, newStatus, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Status updated"));
    }

    @PutMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Ticket>> assignTicket(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body) {
        String staffId = body.get("staffId");
        Ticket ticket = ticketService.assignTicket(
                principal.getTenantId(), ticketId, staffId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Ticket assigned"));
    }
}
