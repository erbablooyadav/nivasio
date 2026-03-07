package com.pgmanager.service;

import com.pgmanager.dto.StaffRequest;
import com.pgmanager.exception.DuplicateResourceException;
import com.pgmanager.exception.ResourceNotFoundException;
import com.pgmanager.model.AppUser;
import com.pgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser createStaff(String tenantId, StaffRequest request) {
        if (userRepository.findByTenantIdAndPhone(tenantId, request.getPhone()).isPresent()) {
            throw new DuplicateResourceException("Staff with phone " + request.getPhone() + " already exists");
        }

        String defaultPassword = request.getPassword() != null ? request.getPassword() : "staff123";

        AppUser staff = AppUser.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .role(AppUser.Role.STAFF)
                .department(request.getDepartment().toUpperCase())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        AppUser saved = userRepository.save(staff);
        log.info("Staff created: {} ({}) for tenant {}", request.getName(), request.getDepartment(), tenantId);
        return saved;
    }

    public List<AppUser> getStaff(String tenantId, String department) {
        if (department != null && !department.isEmpty()) {
            return userRepository.findByTenantIdAndDepartment(tenantId, department.toUpperCase());
        }
        return userRepository.findByTenantIdAndRole(tenantId, AppUser.Role.STAFF);
    }

    public AppUser getStaffById(String tenantId, String id) {
        return userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    public AppUser updateStaff(String tenantId, String id, StaffRequest request) {
        AppUser staff = getStaffById(tenantId, id);
        staff.setName(request.getName());
        staff.setPhone(request.getPhone());
        staff.setEmail(request.getEmail());
        staff.setDepartment(request.getDepartment().toUpperCase());
        staff.setUpdatedAt(Instant.now());
        return userRepository.save(staff);
    }

    public void deactivateStaff(String tenantId, String id) {
        AppUser staff = getStaffById(tenantId, id);
        staff.setActive(false);
        staff.setUpdatedAt(Instant.now());
        userRepository.save(staff);
        log.info("Staff deactivated: {} (tenant: {})", staff.getName(), tenantId);
    }
}
