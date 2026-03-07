package com.pgmanager.controller;

import com.pgmanager.dto.ApiResponse;
import com.pgmanager.dto.StaffRequest;
import com.pgmanager.model.AppUser;
import com.pgmanager.security.UserPrincipal;
import com.pgmanager.service.StaffService;
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
@PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN')")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppUser>> createStaff(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StaffRequest request) {
        AppUser staff = staffService.createStaff(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.ok(staff, "Staff created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppUser>>> getStaff(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String department) {
        List<AppUser> staffList = staffService.getStaff(principal.getTenantId(), department);
        return ResponseEntity.ok(ApiResponse.ok(staffList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppUser>> getStaffById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        AppUser staff = staffService.getStaffById(principal.getTenantId(), id);
        return ResponseEntity.ok(ApiResponse.ok(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppUser>> updateStaff(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody StaffRequest request) {
        AppUser staff = staffService.updateStaff(principal.getTenantId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok(staff, "Staff updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        staffService.deactivateStaff(principal.getTenantId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Staff deactivated"));
    }
}
