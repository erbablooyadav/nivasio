package in.nivasio.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect: validates tenantId on every service method call.
 * Ensures tenantId in JWT matches tenantId param of service methods.
 * NEVER accept tenantId from request body — always from JWT.
 */
@Aspect
@Component
@Slf4j
public class TenantIsolationAspect {

    @Before("execution(* in.nivasio.service..*(String, ..)) && args(tenantId, ..)")
    public void validateTenantId(JoinPoint joinPoint, String tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return; // System call (scheduler, webhook) — no user context
        }

        // SUPER_ADMIN can access any tenant
        if ("SUPER_ADMIN".equals(principal.getRole())) {
            return;
        }

        // Validate tenantId matches JWT
        if (!principal.getTenantId().equals(tenantId)) {
            log.warn("TENANT ISOLATION VIOLATION: user {} (tenant {}) tried to access tenant {}",
                    principal.getUserId(), principal.getTenantId(), tenantId);
            throw new SecurityException("Tenant isolation violation");
        }
    }
}
