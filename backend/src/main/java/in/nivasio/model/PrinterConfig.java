package in.nivasio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "printer_config")
@CompoundIndex(name = "idx_tenant_dept", def = "{'tenantId': 1, 'department': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrinterConfig {
    @Id
    private String id;
    private String tenantId;
    private String propertyId;
    private String department;
    private String connectionType; // USB, LAN, WIFI
    private String ip;
    private int port;
    private boolean active;
}
