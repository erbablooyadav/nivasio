package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.AuditLog;
import in.nivasio.repository.AuditLogRepository;
import in.nivasio.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditRepo;

    @GetMapping
    public ApiResponse<Page<AuditLog>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        String tenantId = user.getTenantId();

        Page<AuditLog> result;
        if (entityType != null && action != null) {
            result = auditRepo.findByTenantIdAndEntityTypeAndAction(tenantId, entityType, action, pageable);
        } else if (entityType != null) {
            result = auditRepo.findByTenantIdAndEntityType(tenantId, entityType, pageable);
        } else if (action != null) {
            result = auditRepo.findByTenantIdAndAction(tenantId, action, pageable);
        } else {
            result = auditRepo.findByTenantId(tenantId, pageable);
        }

        return ApiResponse.ok(result);
    }
}
