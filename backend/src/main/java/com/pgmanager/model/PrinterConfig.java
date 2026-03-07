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
@Document(collection = "printer_config")
@CompoundIndex(name = "tenant_dept", def = "{'tenantId': 1, 'department': 1}", unique = true)
public class PrinterConfig {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String department; // HOUSEKEEPING, LAUNDRY, MAINTENANCE
    private String ip;
    private int port;
    private ConnectionType connectionType; // USB, LAN, WIFI
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;

    public enum ConnectionType {
        USB, LAN, WIFI
    }
}
