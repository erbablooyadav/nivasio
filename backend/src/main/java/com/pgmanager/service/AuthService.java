package com.pgmanager.service;

import com.pgmanager.dto.AuthRequest;
import com.pgmanager.dto.AuthResponse;
import com.pgmanager.dto.RegisterRequest;
import com.pgmanager.exception.BadRequestException;
import com.pgmanager.exception.DuplicateResourceException;
import com.pgmanager.model.AppUser;
import com.pgmanager.model.Subscription;
import com.pgmanager.model.Tenant;
import com.pgmanager.repository.SubscriptionRepository;
import com.pgmanager.repository.TenantRepository;
import com.pgmanager.repository.UserRepository;
import com.pgmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        // Check duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Create tenant
        String tenantId = "TN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .name(request.getPgName())
                .address(request.getPgAddress())
                .contactPhone(request.getPhone())
                .contactEmail(request.getEmail())
                .plan("FREE")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        tenantRepository.save(tenant);

        // Create admin user
        AppUser admin = AppUser.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AppUser.Role.TENANT_ADMIN)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        AppUser savedUser = userRepository.save(admin);

        // Create free subscription
        Subscription sub = Subscription.builder()
                .tenantId(tenantId)
                .plan("FREE")
                .paymentStatus("ACTIVE")
                .maxRooms(10)
                .maxDepartments(2)
                .startDate(Instant.now())
                .createdAt(Instant.now())
                .build();
        subscriptionRepository.save(sub);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                savedUser.getId(), tenantId, savedUser.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        log.info("New tenant registered: {} ({})", request.getPgName(), tenantId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .tenantId(tenantId)
                .name(savedUser.getName())
                .role(savedUser.getRole().name())
                .expiresIn(86400)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByPhone(request.getIdentifier()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("User logged in: {} (tenant: {})", user.getEmail(), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .name(user.getName())
                .role(user.getRole().name())
                .expiresIn(86400)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .name(user.getName())
                .role(user.getRole().name())
                .expiresIn(86400)
                .build();
    }
}
