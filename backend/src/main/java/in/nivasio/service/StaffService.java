package in.nivasio.service;

import in.nivasio.dto.StaffRequest;
import in.nivasio.exception.*;
import in.nivasio.model.Staff;
import in.nivasio.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final AuditService auditService;

    public Staff createStaff(String tenantId, StaffRequest request) {
        staffRepo.findByTenantIdAndPhone(tenantId, request.getPhone()).ifPresent(s -> {
            throw new DuplicateResourceException("Staff with phone " + request.getPhone() + " already exists");
        });

        Staff staff = Staff.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .department(request.getDepartment())
                .role("STAFF")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        staff = staffRepo.save(staff);
        auditService.log(tenantId, "STAFF", staff.getId(), "CREATED", null, staff.getName());
        return staff;
    }

    public List<Staff> getStaff(String tenantId, String department) {
        if (department != null) {
            return staffRepo.findByTenantIdAndDepartmentAndActiveTrue(tenantId, department);
        }
        return staffRepo.findByTenantIdAndActiveTrue(tenantId);
    }

    public Staff getStaffById(String tenantId, String id) {
        return staffRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    public Staff updateStaff(String tenantId, String id, StaffRequest request) {
        Staff staff = getStaffById(tenantId, id);
        staff.setName(request.getName());
        staff.setPhone(request.getPhone());
        staff.setEmail(request.getEmail());
        staff.setDepartment(request.getDepartment());
        staff.setUpdatedAt(Instant.now());
        return staffRepo.save(staff);
    }

    public void deactivateStaff(String tenantId, String id) {
        Staff staff = getStaffById(tenantId, id);
        staff.setActive(false);
        staff.setUpdatedAt(Instant.now());
        staffRepo.save(staff);
        auditService.log(tenantId, "STAFF", id, "DEACTIVATED", null, staff.getName());
    }
}
