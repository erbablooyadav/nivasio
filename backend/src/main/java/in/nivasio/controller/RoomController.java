package in.nivasio.controller;

import in.nivasio.dto.*;
import in.nivasio.model.Room;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Room>> create(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.createRoom(user.getTenantId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Room>>> list(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getRooms(user.getTenantId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        roomService.deleteRoom(user.getTenantId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Room deleted"));
    }
}
