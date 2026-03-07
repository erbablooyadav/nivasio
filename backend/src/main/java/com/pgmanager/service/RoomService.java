package com.pgmanager.service;

import com.pgmanager.dto.RoomRequest;
import com.pgmanager.exception.DuplicateResourceException;
import com.pgmanager.exception.ResourceNotFoundException;
import com.pgmanager.model.Room;
import com.pgmanager.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    public Room createRoom(String tenantId, RoomRequest request) {
        if (roomRepository.findByTenantIdAndRoomNo(tenantId, request.getRoomNo()).isPresent()) {
            throw new DuplicateResourceException("Room " + request.getRoomNo() + " already exists");
        }

        Room room = Room.builder()
                .tenantId(tenantId)
                .roomNo(request.getRoomNo())
                .floor(request.getFloor())
                .capacity(request.getCapacity() > 0 ? request.getCapacity() : 1)
                .type(request.getType() != null ? request.getType() : "SINGLE")
                .occupied(false)
                .createdAt(Instant.now())
                .build();

        Room saved = roomRepository.save(room);
        log.info("Room created: {} (tenant: {})", request.getRoomNo(), tenantId);
        return saved;
    }

    public List<Room> getRooms(String tenantId) {
        return roomRepository.findByTenantId(tenantId);
    }

    public Room getRoomById(String tenantId, String id) {
        return roomRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    public void deleteRoom(String tenantId, String id) {
        Room room = getRoomById(tenantId, id);
        roomRepository.delete(room);
        log.info("Room deleted: {} (tenant: {})", room.getRoomNo(), tenantId);
    }
}
