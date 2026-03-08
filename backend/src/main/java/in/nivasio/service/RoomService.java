package in.nivasio.service;

import in.nivasio.dto.RoomRequest;
import in.nivasio.exception.*;
import in.nivasio.model.Room;
import in.nivasio.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepo;

    public Room createRoom(String tenantId, RoomRequest request) {
        roomRepo.findByTenantIdAndRoomNo(tenantId, request.getRoomNo()).ifPresent(r -> {
            throw new DuplicateResourceException("Room " + request.getRoomNo() + " already exists");
        });

        Room room = Room.builder()
                .tenantId(tenantId)
                .propertyId(request.getPropertyId())
                .roomNo(request.getRoomNo())
                .floor(request.getFloor())
                .type(request.getType() != null ? request.getType() : "SINGLE")
                .capacity(request.getCapacity())
                .occupied(0)
                .status("VACANT")
                .createdAt(Instant.now())
                .build();

        return roomRepo.save(room);
    }

    public List<Room> getRooms(String tenantId) {
        return roomRepo.findByTenantId(tenantId);
    }

    public void deleteRoom(String tenantId, String id) {
        Room room = roomRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepo.delete(room);
    }
}
