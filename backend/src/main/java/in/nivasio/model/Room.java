package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rooms")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tenant_room", def = "{'tenantId': 1, 'roomNo': 1}", unique = true),
        @CompoundIndex(name = "idx_tenant_property", def = "{'tenantId': 1, 'propertyId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String roomNo;
    private int floor;
    private String type; // SINGLE, DOUBLE, TRIPLE, DORMITORY
    private int capacity;
    private int occupied;
    private String status; // VACANT, OCCUPIED, MAINTENANCE
    private Instant createdAt;
}
