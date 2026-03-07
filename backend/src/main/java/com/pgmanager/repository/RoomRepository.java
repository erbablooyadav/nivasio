package com.pgmanager.repository;

import com.pgmanager.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByTenantId(String tenantId);

    Optional<Room> findByTenantIdAndRoomNo(String tenantId, String roomNo);

    Optional<Room> findByTenantIdAndId(String tenantId, String id);

    long countByTenantId(String tenantId);
}
