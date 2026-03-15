package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.Onboarding;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    public ApiResponse<Onboarding> submit(@AuthenticationPrincipal UserPrincipal user,
            @RequestBody Onboarding onboarding) {
        return ApiResponse.ok(onboardingService.submit(user.getTenantId(), onboarding));
    }

    @GetMapping
    public ApiResponse<Page<Onboarding>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(onboardingService.list(user.getTenantId(), status, page, size));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Onboarding> approve(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        return ApiResponse.ok(onboardingService.approve(user.getTenantId(), id, user.getUserId()));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Onboarding> reject(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return ApiResponse.ok(onboardingService.reject(user.getTenantId(), id, reason, user.getUserId()));
    }
}
