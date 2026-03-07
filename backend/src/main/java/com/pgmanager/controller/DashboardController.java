package com.pgmanager.controller;

import com.pgmanager.dto.ApiResponse;
import com.pgmanager.dto.DashboardStats;
import com.pgmanager.security.UserPrincipal;
import com.pgmanager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        DashboardStats stats = dashboardService.getStats(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
