package in.nivasio.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Verifies HMAC-SHA256 signatures on webhooks (Meta WhatsApp, Razorpay).
 */
@Component
@Slf4j
public class WebhookSignatureVerifier {

    public boolean verifyMetaSignature(String payload, String signature, String appSecret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            String expectedSig = signature.substring(7);
            String computed = hmacSha256(payload, appSecret);
            return constantTimeEquals(expectedSig, computed);
        } catch (Exception e) {
            log.error("Meta signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyRazorpaySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            String computed = hmacSha256(payload, secret);
            return constantTimeEquals(signature, computed);
        } catch (Exception e) {
            log.error("Razorpay signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length())
            return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
