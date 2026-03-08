package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "staff")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_dept", def = "{'tenantId': 1, 'department': 1}"),
        @CompoundIndex(name = "idx_tenant_phone", def = "{'tenantId': 1, 'phone': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String name;
    private String phone;
    private String email;
    private String role; // STAFF
    private String department; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, FOOD, SECURITY, GENERAL
    private String fcmToken;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
