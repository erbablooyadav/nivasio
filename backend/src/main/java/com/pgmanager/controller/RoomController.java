package com.pgmanager.controller;

import com.pgmanager.dto.ApiResponse;
import com.pgmanager.dto.RoomRequest;
import com.pgmanager.model.Room;
import com.pgmanager.security.UserPrincipal;
import com.pgmanager.service.RoomService;
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
@PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN')")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<Room>> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RoomRequest request) {
        Room room = roomService.createRoom(principal.getTenantId(), request);
        return ResponseEntity.ok(ApiResponse.ok(room, "Room created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Room>>> getRooms(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Room> rooms = roomService.getRooms(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        roomService.deleteRoom(principal.getTenantId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Room deleted"));
    }
}
