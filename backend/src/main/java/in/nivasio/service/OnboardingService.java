package in.nivasio.service;

import in.nivasio.exception.*;
import in.nivasio.model.Onboarding;
import in.nivasio.model.Resident;
import in.nivasio.repository.OnboardingRepository;
import in.nivasio.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final OnboardingRepository onboardingRepo;
    private final ResidentRepository residentRepo;
    private final AuditService auditService;

    public Onboarding submit(String tenantId, Onboarding onboarding) {
        onboarding.setTenantId(tenantId);
        onboarding.setStatus("PENDING");
        onboarding.setPoliceVerificationStatus("PENDING");
        onboarding.setSubmittedAt(Instant.now());
        Onboarding saved = onboardingRepo.save(onboarding);
        auditService.log(tenantId, "ONBOARDING", saved.getId(), "SUBMITTED", null, saved.getFullName());
        log.info("Onboarding submitted for {}", saved.getFullName());
        return saved;
    }

    public Page<Onboarding> list(String tenantId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        if (status != null) {
            return onboardingRepo.findByTenantIdAndStatus(tenantId, status, pageable);
        }
        return onboardingRepo.findByTenantId(tenantId, pageable);
    }

    public Onboarding approve(String tenantId, String id, String reviewerId) {
        Onboarding ob = onboardingRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));

        if (!"PENDING".equals(ob.getStatus())) {
            throw new BadRequestException("Can only approve PENDING onboardings");
        }

        ob.setStatus("APPROVED");
        ob.setReviewedBy(reviewerId);
        ob.setReviewedAt(Instant.now());
        ob.setCompletedAt(Instant.now());
        onboardingRepo.save(ob);

        // Auto-create resident record
        Resident resident = Resident.builder()
                .tenantId(tenantId)
                .propertyId(ob.getPropertyId())
                .name(ob.getFullName())
                .phone(ob.getPhone())
                .email(ob.getEmail())
                .roomNo(ob.getRoomNo())
                .idProofType(ob.getIdProofType())
                .idProofUrl(ob.getIdProofUrl())
                .emergencyContact(ob.getEmergencyContactPhone())
                .active(true)
                .status("ACTIVE")
                .moveInDate(Instant.now())
                .createdAt(Instant.now())
                .build();
        residentRepo.save(resident);

        auditService.log(tenantId, "ONBOARDING", id, "APPROVED", reviewerId, null);
        return ob;
    }

    public Onboarding reject(String tenantId, String id, String reason, String reviewerId) {
        Onboarding ob = onboardingRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));

        if (!"PENDING".equals(ob.getStatus())) {
            throw new BadRequestException("Can only reject PENDING onboardings");
        }

        ob.setStatus("REJECTED");
        ob.setRejectionReason(reason);
        ob.setReviewedBy(reviewerId);
        ob.setReviewedAt(Instant.now());
        onboardingRepo.save(ob);

        auditService.log(tenantId, "ONBOARDING", id, "REJECTED", reviewerId, reason);
        return ob;
    }
}
