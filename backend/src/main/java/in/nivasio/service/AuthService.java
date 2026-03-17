package in.nivasio.service;

import in.nivasio.dto.AuthResponse;
import in.nivasio.dto.OtpVerifyRequest;
import in.nivasio.dto.RegisterRequest;
import in.nivasio.model.*;
import in.nivasio.repository.*;
import in.nivasio.security.JwtTokenProvider;
import in.nivasio.exception.*;
import in.nivasio.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${app.whatsapp.mock-mode:true}")
    private boolean mockMode;
    private final TenantRepository tenantRepo;
    private final StaffRepository staffRepo;
    private final ResidentRepository residentRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final PropertyRepository propertyRepo;
    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redis;
    private final WhatsAppService whatsAppService;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempt:";

    /**
     * Send OTP to phone number (mocked — logs OTP to console in dev).
     */
    public void requestOtp(String phone) {
        // Check brute force: max 3 attempts per phone per 30 min
        String attemptKey = OTP_ATTEMPT_PREFIX + phone;
        String attempts = redis.opsForValue().get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts) >= 3) {
            throw new BadRequestException("Too many OTP attempts. Try again after 30 minutes.");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(999999));

        // Store in Redis with 5 min TTL
        redis.opsForValue().set(OTP_PREFIX + phone, otp, Duration.ofMinutes(5));

        // Increment attempt counter
        redis.opsForValue().increment(attemptKey);
        redis.expire(attemptKey, Duration.ofMinutes(30));

        if (mockMode) {
            log.info("[DEV OTP] Phone: {}, OTP: {}", maskPhone(phone), otp);
        } else {
            whatsAppService.sendOtp(phone, otp).block();
            log.info("[WA OTP] Phone: {}, OTP: {}", maskPhone(phone), otp);
        }
    }

    /**
     * Verify OTP and return JWT tokens.
     */
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String storedOtp = redis.opsForValue().get(OTP_PREFIX + request.getPhone());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        // Delete used OTP
        redis.delete(OTP_PREFIX + request.getPhone());
        redis.delete(OTP_ATTEMPT_PREFIX + request.getPhone());

        // Find user by phone — could be tenant owner, staff, or resident
        Optional<Tenant> tenantOpt = tenantRepo.findByOwnerPhone(request.getPhone());
        if (tenantOpt.isPresent()) {
            Tenant tenant = tenantOpt.get();
            return buildAuthResponse(tenant.getId(), tenant.getTenantId(),
                    tenant.getOwnerName(), "PROPERTY_ADMIN", null);
        }

        Optional<Staff> staffOpt = staffRepo.findByPhone(request.getPhone());
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            return buildAuthResponse(staff.getId(), staff.getTenantId(),
                    staff.getName(), "STAFF", staff.getPropertyId());
        }

        Optional<Resident> residentOpt = residentRepo.findByPhone(request.getPhone());
        if (residentOpt.isPresent()) {
            Resident resident = residentOpt.get();
            return buildAuthResponse(resident.getId(), resident.getTenantId(),
                    resident.getName(), "RESIDENT", resident.getPropertyId());
        }

        throw new ResourceNotFoundException("Phone number not registered. Contact your property admin.");
    }

    /**
     * Register new property owner.
     */
    public AuthResponse register(RegisterRequest request) {
        if (tenantRepo.existsByOwnerPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        String tenantId = UUID.randomUUID().toString().substring(0, 8);
        String propertyId = UUID.randomUUID().toString().substring(0, 8);

        // Create tenant
        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .name(request.getPgName())
                .ownerName(request.getName())
                .ownerPhone(request.getPhone())
                .ownerEmail(request.getEmail())
                .address(request.getPgAddress())
                .plan("FREE")
                .defaultLanguage("en")
                .active(true)
                .slaConfig(Map.of(
                        "HOUSEKEEPING", 120,
                        "LAUNDRY", 60,
                        "MAINTENANCE", 240,
                        "FOOD", 240,
                        "GENERAL", 480))
                .createdAt(Instant.now())
                .build();
        tenantRepo.save(tenant);

        // Create default property
        Property property = Property.builder()
                .tenantId(tenantId)
                .propertyId(propertyId)
                .name(request.getPgName())
                .address(request.getPgAddress())
                .type(request.getPgType() != null ? request.getPgType() : "PG")
                .active(true)
                .createdAt(Instant.now())
                .build();
        propertyRepo.save(property);

        // Create subscription
        Subscription sub = Subscription.builder()
                .tenantId(tenantId)
                .plan("FREE")
                .status("ACTIVE")
                .startDate(Instant.now())
                .autoRenew(false)
                .createdAt(Instant.now())
                .build();
        subscriptionRepo.save(sub);

        return buildAuthResponse(tenant.getId(), tenantId,
                request.getName(), "PROPERTY_ADMIN", propertyId);
    }

    /**
     * Refresh access token using refresh token (from HttpOnly cookie).
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String tokenType = tokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        // Check if token is blacklisted
        if (Boolean.TRUE.equals(redis.hasKey("blacklist:" + refreshToken))) {
            throw new UnauthorizedException("Token has been revoked");
        }

        String userId = tokenProvider.getUserId(refreshToken);
        String tenantId = tokenProvider.getTenantId(refreshToken);

        // Blacklist old refresh token (rotation)
        redis.opsForValue().set("blacklist:" + refreshToken, "revoked",
                Duration.ofDays(7));

        // Find user to get current role
        Optional<Tenant> tenantOpt = tenantRepo.findById(userId);
        if (tenantOpt.isPresent()) {
            return buildAuthResponse(userId, tenantId,
                    tenantOpt.get().getOwnerName(), "PROPERTY_ADMIN", null);
        }

        throw new UnauthorizedException("User not found");
    }

    private AuthResponse buildAuthResponse(String userId, String tenantId,
            String name, String role, String propertyId) {
        String accessToken = tokenProvider.generateAccessToken(userId, tenantId, role, propertyId);
        // Refresh token will be set as HttpOnly cookie in the controller
        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(userId)
                .tenantId(tenantId)
                .name(name)
                .role(role)
                .propertyId(propertyId)
                .build();
    }

    public String generateRefreshToken(String userId, String tenantId) {
        return tokenProvider.generateRefreshToken(userId, tenantId);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
