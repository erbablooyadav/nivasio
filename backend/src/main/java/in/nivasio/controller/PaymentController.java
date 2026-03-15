package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.RentRecord;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ApiResponse<Map<String, String>> createOrder(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, String> body) {
        String rentId = body.get("rentId");
        return ApiResponse.ok(paymentService.createOrder(user.getTenantId(), rentId));
    }

    @PostMapping("/verify")
    public ApiResponse<RentRecord> verify(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, String> body) {
        return ApiResponse.ok(paymentService.verifyPayment(
                user.getTenantId(),
                body.get("rentId"),
                body.get("razorpay_payment_id"),
                body.get("razorpay_order_id"),
                body.get("razorpay_signature")));
    }
}
