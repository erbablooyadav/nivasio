package in.nivasio.controller;

import in.nivasio.dto.*;
import in.nivasio.model.Ticket;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> create(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody TicketRequest request) {
        Ticket ticket = ticketService.createTicket(user.getTenantId(), request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Ticket created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Ticket>>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String dept = "STAFF".equals(user.getRole()) ? user.getDepartment() : department;
        Page<Ticket> tickets = ticketService.getTickets(user.getTenantId(), status, dept, roomNo, assignedTo, page,
                size);
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<Ticket>> getById(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String ticketId) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getTicketById(user.getTenantId(), ticketId)));
    }

    @PutMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String ticketId,
            @RequestParam String status) {
        Ticket ticket = ticketService.updateStatus(user.getTenantId(), ticketId, status, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @PutMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Ticket>> assign(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String ticketId,
            @RequestParam String staffId) {
        Ticket ticket = ticketService.assignTicket(user.getTenantId(), ticketId, staffId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }
}
