package in.nivasio.controller;

import in.nivasio.security.UserPrincipal;
import in.nivasio.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrService;

    @GetMapping("/room/{roomNo}")
    public ResponseEntity<byte[]> roomQr(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String roomNo) {
        byte[] qr = qrService.generateRoomQr(user.getTenantId(), roomNo);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qr_room_" + roomNo + ".png")
                .body(qr);
    }

    @GetMapping("/floor/{floor}")
    public ResponseEntity<byte[]> floorQrZip(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable int floor) {
        byte[] zip = qrService.generateFloorQrZip(user.getTenantId(), floor);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr_floor_" + floor + ".zip")
                .body(zip);
    }

    @GetMapping("/all")
    public ResponseEntity<byte[]> allRoomsQrZip(@AuthenticationPrincipal UserPrincipal user) {
        byte[] zip = qrService.generateAllRoomsQrZip(user.getTenantId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr_all_rooms.zip")
                .body(zip);
    }
}
