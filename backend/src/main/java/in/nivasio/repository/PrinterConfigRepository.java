package in.nivasio.repository;

import in.nivasio.model.PrinterConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PrinterConfigRepository extends MongoRepository<PrinterConfig, String> {
    Optional<PrinterConfig> findByTenantIdAndDepartment(String tenantId, String department);
}
