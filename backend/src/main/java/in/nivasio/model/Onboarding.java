package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "onboardings")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_status", def = "{'tenantId': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_tenant_resident", def = "{'tenantId': 1, 'residentId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Onboarding {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String residentId;

    // Personal info
    private String fullName;
    private String phone;
    private String email;
    private String roomNo;
    private String emergencyContactName;
    private String emergencyContactPhone;

    // Documents (URLs to uploaded files — never store raw files in DB)
    private String idProofType; // AADHAAR, PAN, PASSPORT
    private String idProofUrl;
    private String agreementUrl; // Generated PDF
    private String policeVerificationStatus; // PENDING, SUBMITTED, VERIFIED

    private String status; // PENDING, APPROVED, REJECTED
    private String reviewedBy;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant reviewedAt;
    private Instant completedAt;
}
