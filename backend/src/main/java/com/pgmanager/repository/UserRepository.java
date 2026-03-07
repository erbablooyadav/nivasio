package com.pgmanager.repository;

import com.pgmanager.model.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByTenantIdAndPhone(String tenantId, String phone);

    Optional<AppUser> findByTenantIdAndId(String tenantId, String id);

    Optional<AppUser> findByPhone(String phone);

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByTenantIdAndRole(String tenantId, AppUser.Role role);

    List<AppUser> findByTenantIdAndDepartment(String tenantId, String department);

    List<AppUser> findByTenantId(String tenantId);

    long countByTenantId(String tenantId);
}
