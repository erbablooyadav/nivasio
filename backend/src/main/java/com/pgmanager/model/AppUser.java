package com.pgmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
@CompoundIndex(name = "tenant_phone", def = "{'tenantId': 1, 'phone': 1}", unique = true)
public class AppUser {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String name;
    private String phone;
    private String whatsappNo;
    private String email;
    private String password; // BCrypt encoded, for admin/staff login
    private String roomId;
    private String roomNo;

    private Role role; // SUPER_ADMIN, TENANT_ADMIN, STAFF, TENANT
    private String department; // For staff: HOUSEKEEPING, LAUNDRY, MAINTENANCE, FOOD
    private List<String> departments; // Staff can belong to multiple departments

    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Role {
        SUPER_ADMIN, TENANT_ADMIN, STAFF, TENANT
    }
}
