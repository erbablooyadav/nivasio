package in.nivasio.service;

import in.nivasio.exception.*;
import in.nivasio.model.Resident;
import in.nivasio.repository.ResidentRepository;
import in.nivasio.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResidentService {

    private final ResidentRepository residentRepo;
    private final RoomRepository roomRepo;
    private final AuditService auditService;

    public Resident create(String tenantId, Resident resident, String userId) {
        // Security: enforce tenantId from JWT
        resident.setTenantId(tenantId);
        resident.setMoveInDate(Instant.now());
        resident.setActive(true);

        // Validate phone uniqueness within tenant
        Optional<Resident> existing = residentRepo.findByTenantIdAndPhone(tenantId, resident.getPhone());
        if (existing.isPresent() && existing.get().isActive()) {
            throw new DuplicateResourceException(
                    "Resident with phone " + maskPhone(resident.getPhone()) + " already exists");
        }

        Resident saved = residentRepo.save(resident);

        // Update room occupancy
        if (resident.getRoomNo() != null) {
            updateRoomOccupancy(tenantId, resident.getRoomNo(), 1);
        }

        auditService.log(tenantId, "RESIDENT", saved.getId(), "CREATED", userId, null);
        return saved;
    }

    public Page<Resident> list(String tenantId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "moveInDate"));
        if (search != null && !search.isBlank()) {
            return residentRepo.findByTenantIdAndNameContainingIgnoreCase(tenantId, search, pageable);
        }
        return residentRepo.findByTenantIdAndActiveTrue(tenantId, pageable);
    }

    public Resident getById(String tenantId, String id) {
        return residentRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    public List<Resident> getByRoom(String tenantId, String roomNo) {
        return residentRepo.findByTenantIdAndRoomNoAndActiveTrue(tenantId, roomNo);
    }

    public Resident update(String tenantId, String id, Resident updated, String userId) {
        Resident resident = getById(tenantId, id);
        if (updated.getName() != null)
            resident.setName(updated.getName());
        if (updated.getEmail() != null)
            resident.setEmail(updated.getEmail());
        if (updated.getEmergencyContact() != null)
            resident.setEmergencyContact(updated.getEmergencyContact());
        if (updated.getLanguagePreference() != null)
            resident.setLanguagePreference(updated.getLanguagePreference());
        Resident saved = residentRepo.save(resident);
        auditService.log(tenantId, "RESIDENT", id, "UPDATED", userId, null);
        return saved;
    }

    public Resident moveOut(String tenantId, String id, String userId) {
        Resident resident = getById(tenantId, id);
        resident.setActive(false);
        resident.setMoveOutDate(Instant.now());
        residentRepo.save(resident);

        // Decrement room occupancy
        if (resident.getRoomNo() != null) {
            updateRoomOccupancy(tenantId, resident.getRoomNo(), -1);
        }

        auditService.log(tenantId, "RESIDENT", id, "MOVED_OUT", userId, null);
        return resident;
    }

    private void updateRoomOccupancy(String tenantId, String roomNo, int delta) {
        roomRepo.findByTenantIdAndRoomNo(tenantId, roomNo).ifPresent(room -> {
            int newOccupied = Math.max(0, room.getOccupied() + delta);
            room.setOccupied(newOccupied);
            room.setStatus(newOccupied >= room.getCapacity() ? "OCCUPIED" : newOccupied > 0 ? "OCCUPIED" : "VACANT");
            roomRepo.save(room);
        });
    }

    /** Security: mask phone numbers for logging */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
