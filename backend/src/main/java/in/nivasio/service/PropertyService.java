package in.nivasio.service;

import in.nivasio.exception.*;
import in.nivasio.model.Property;
import in.nivasio.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepo;
    private final AuditService auditService;

    public Property create(String tenantId, Property property, String userId) {
        // Security: enforce tenantId from JWT, never from request body
        property.setTenantId(tenantId);
        property.setCreatedAt(Instant.now());
        property.setActive(true);
        Property saved = propertyRepo.save(property);
        auditService.log(tenantId, "PROPERTY", saved.getId(), "CREATED", userId, saved.getName());
        log.info("Property created: {} for tenant {}", saved.getName(), tenantId);
        return saved;
    }

    public List<Property> listByTenant(String tenantId) {
        return propertyRepo.findByTenantId(tenantId);
    }

    public Property getById(String tenantId, String propertyId) {
        return propertyRepo.findByIdAndTenantId(propertyId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
    }

    public Property update(String tenantId, String propertyId, Property updated, String userId) {
        Property property = getById(tenantId, propertyId);
        if (updated.getName() != null)
            property.setName(updated.getName());
        if (updated.getAddress() != null)
            property.setAddress(updated.getAddress());
        if (updated.getCity() != null)
            property.setCity(updated.getCity());
        if (updated.getType() != null)
            property.setType(updated.getType());
        if (updated.getTotalFloors() > 0)
            property.setTotalFloors(updated.getTotalFloors());
        Property saved = propertyRepo.save(property);
        auditService.log(tenantId, "PROPERTY", propertyId, "UPDATED", userId, null);
        return saved;
    }

    public void deactivate(String tenantId, String propertyId, String userId) {
        Property property = getById(tenantId, propertyId);
        property.setActive(false);
        propertyRepo.save(property);
        auditService.log(tenantId, "PROPERTY", propertyId, "DEACTIVATED", userId, null);
    }
}
