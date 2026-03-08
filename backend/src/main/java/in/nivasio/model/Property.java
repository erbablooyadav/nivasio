package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "properties")
@CompoundIndex(name = "idx_tenant", def = "{'tenantId': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String name;
    private String address;
    private String type; // PG, HOSTEL, APARTMENT, SOCIETY, CO_LIVING
    private int totalRooms;
    private List<String> amenities;
    private boolean active;
    private Instant createdAt;
}
