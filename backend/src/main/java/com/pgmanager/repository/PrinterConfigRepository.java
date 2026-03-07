package com.pgmanager.repository;

import com.pgmanager.model.PrinterConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PrinterConfigRepository extends MongoRepository<PrinterConfig, String> {
    List<PrinterConfig> findByTenantId(String tenantId);

    Optional<PrinterConfig> findByTenantIdAndDepartment(String tenantId, String department);
}
