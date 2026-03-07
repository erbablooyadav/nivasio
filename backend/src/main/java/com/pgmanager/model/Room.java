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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rooms")
@CompoundIndex(name = "tenant_room", def = "{'tenantId': 1, 'roomNo': 1}", unique = true)
public class Room {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String roomNo;
    private int floor;
    private int capacity;
    private String type; // SINGLE, DOUBLE, TRIPLE, DORMITORY
    private boolean occupied;
    private Instant createdAt;
}
