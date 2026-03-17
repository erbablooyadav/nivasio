package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "residents")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_phone", def = "{'tenantId': 1, 'phone': 1}", unique = true),
        @CompoundIndex(name = "idx_tenant_room", def = "{'tenantId': 1, 'roomId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resident {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String roomId;
    private String roomNo;
    private String name;
    private String phone;
    private String whatsappNo;
    private String email;
    private String idProofUrl;
    private String idProofType; // AADHAAR, PAN, PASSPORT
    private String emergencyContact;
    private String languagePreference; // en, hi
    private boolean active;
    private String status; // ACTIVE, MOVED_OUT, SUSPENDED
    private Instant moveInDate;
    private Instant moveOutDate;
    private Instant createdAt;
    private Instant updatedAt;
}
