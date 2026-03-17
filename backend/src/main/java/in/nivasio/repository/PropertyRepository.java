package in.nivasio.repository;

import in.nivasio.model.Property;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PropertyRepository extends MongoRepository<Property, String> {
    List<Property> findByTenantId(String tenantId);

    List<Property> findByTenantIdAndActiveTrue(String tenantId);

    java.util.Optional<Property> findByIdAndTenantId(String id, String tenantId);
}
