package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.RentRecord;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.RentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rent")
@RequiredArgsConstructor
public class RentController {

    private final RentService rentService;

    @GetMapping
    public ApiResponse<Page<RentRecord>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(rentService.list(user.getTenantId(), month, status, page, size));
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Object> body) {
        String month = (String) body.get("month");
        double amount = ((Number) body.getOrDefault("amount", 5000)).doubleValue();
        int count = rentService.generateMonthlyRent(user.getTenantId(), month, amount);
        return ApiResponse.ok(Map.of("generated", count, "month", month));
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<RentRecord> pay(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String mode = body.getOrDefault("paymentMode", "CASH");
        String txnId = body.get("transactionId");
        return ApiResponse.ok(rentService.markPaid(user.getTenantId(), id, mode, txnId, user.getUserId()));
    }

    @GetMapping("/overdue-count")
    public ApiResponse<Map<String, Long>> overdueCount(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(Map.of("count", rentService.countOverdue(user.getTenantId())));
    }
}
