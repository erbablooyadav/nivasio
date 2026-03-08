package in.nivasio.repository;

import in.nivasio.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByTenantId(String tenantId);

    List<Room> findByTenantIdAndPropertyId(String tenantId, String propertyId);

    Optional<Room> findByTenantIdAndRoomNo(String tenantId, String roomNo);

    Optional<Room> findByIdAndTenantId(String id, String tenantId);

    long countByTenantId(String tenantId);
}
