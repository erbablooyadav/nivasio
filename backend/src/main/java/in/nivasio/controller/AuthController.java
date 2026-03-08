package in.nivasio.controller;

import in.nivasio.dto.*;
import in.nivasio.service.AuthService;
import in.nivasio.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<String>> requestOtp(@Valid @RequestBody AuthRequest request) {
        authService.requestOtp(request.getPhone());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request,
            HttpServletResponse response) {
        AuthResponse authResp = authService.verifyOtp(request);

        // Set refresh token as HttpOnly cookie
        String refreshToken = authService.generateRefreshToken(authResp.getUserId(), authResp.getTenantId());
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (tokenProvider.getRefreshExpiryMs() / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.ok(authResp, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResp = authService.register(request);

        String refreshToken = authService.generateRefreshToken(authResp.getUserId(), authResp.getTenantId());
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (tokenProvider.getRefreshExpiryMs() / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.ok(authResp, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("No refresh token", "ERR_NO_TOKEN"));
        }

        AuthResponse authResp = authService.refreshToken(refreshToken);

        // Rotate: issue new refresh token
        String newRefresh = authService.generateRefreshToken(authResp.getUserId(), authResp.getTenantId());
        Cookie cookie = new Cookie("refreshToken", newRefresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (tokenProvider.getRefreshExpiryMs() / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.ok(authResp));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/api/v1/auth/refresh");
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName()))
                    return c.getValue();
            }
        }
        return null;
    }
}
