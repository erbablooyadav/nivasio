package in.nivasio.service;

import in.nivasio.model.RentRecord;
import in.nivasio.repository.RentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Razorpay payment integration — MOCK implementation.
 * In production, replace with real Razorpay Java SDK calls.
 * Security: never log full payment details, only transaction IDs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RentRecordRepository rentRepo;
    private final AuditService auditService;

    /**
     * Create a mock Razorpay order for a rent record.
     * In production: call Razorpay.Orders.create()
     */
    public Map<String, String> createOrder(String tenantId, String rentId) {
        RentRecord rent = rentRepo.findByIdAndTenantId(rentId, tenantId)
                .orElseThrow(() -> new in.nivasio.exception.ResourceNotFoundException("Rent record not found"));

        if ("PAID".equals(rent.getStatus())) {
            throw new in.nivasio.exception.BadRequestException("Already paid");
        }

        // Mock Razorpay order
        String orderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[MOCK RAZORPAY] Order created: {} for ₹{}", orderId, rent.getAmount());

        return Map.of(
                "orderId", orderId,
                "amount", String.valueOf((int) (rent.getAmount() * 100)), // paise
                "currency", "INR",
                "key", "rzp_test_nivasio_mock",
                "name", "Nivasio",
                "description", "Rent for " + rent.getMonth() + " - Room " + rent.getRoomNo());
    }

    /**
     * Verify payment callback (mock: always succeeds).
     * In production: verify Razorpay signature using HMAC-SHA256.
     * Security: NEVER trust client-side payment confirmation alone.
     */
    public RentRecord verifyPayment(String tenantId, String rentId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature) {
        // Production: verify signature
        // String expectedSignature = HmacUtils.hmacSha256Hex(razorpaySecret,
        // razorpayOrderId + "|" + razorpayPaymentId);
        // if (!expectedSignature.equals(razorpaySignature)) throw new
        // UnauthorizedException("Invalid signature");

        RentRecord rent = rentRepo.findByIdAndTenantId(rentId, tenantId)
                .orElseThrow(() -> new in.nivasio.exception.ResourceNotFoundException("Rent record not found"));

        rent.setStatus("PAID");
        rent.setPaidDate(Instant.now());
        rent.setPaymentMode("RAZORPAY");
        rent.setTransactionId(razorpayPaymentId);
        rentRepo.save(rent);

        auditService.log(tenantId, "RENT", rentId, "PAID",
                null, "Razorpay: " + razorpayPaymentId);

        log.info("[MOCK RAZORPAY] Payment verified: {} for rent {}", razorpayPaymentId, rentId);
        return rent;
    }
}
