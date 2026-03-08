package in.nivasio.controller;

import in.nivasio.dto.*;
import in.nivasio.model.Staff;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<ApiResponse<Staff>> create(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.createStaff(user.getTenantId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Staff>>> list(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.getStaff(user.getTenantId(), department)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Staff>> update(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.updateStaff(user.getTenantId(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        staffService.deactivateStaff(user.getTenantId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Staff deactivated"));
    }
}
