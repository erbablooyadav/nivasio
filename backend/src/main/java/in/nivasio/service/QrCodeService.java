package in.nivasio.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import in.nivasio.model.Room;
import in.nivasio.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * QR Code service: generates per-room QR codes for resident ticket submission.
 * QR links point to WhatsApp bot or web form — never encode sensitive data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final RoomRepository roomRepo;

    @Value("${app.whatsapp.phone-number-id:}")
    private String waPhoneNumberId;

    private static final int QR_SIZE = 300;

    /**
     * Generate QR code image bytes for a single room.
     * QR content is a WhatsApp link or web URL — no sensitive data encoded.
     */
    public byte[] generateRoomQr(String tenantId, String roomNo) {
        String qrContent = buildQrContent(tenantId, roomNo);
        return generateQrBytes(qrContent);
    }

    /**
     * Generate ZIP containing QR codes for all rooms on a specific floor.
     */
    public byte[] generateFloorQrZip(String tenantId, int floor) {
        List<Room> rooms = roomRepo.findByTenantId(tenantId).stream()
                .filter(r -> r.getFloor() == floor)
                .toList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Room room : rooms) {
                byte[] qr = generateRoomQr(tenantId, room.getRoomNo());
                zos.putNextEntry(new ZipEntry("room_" + room.getRoomNo() + ".png"));
                zos.write(qr);
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to create QR ZIP for floor {}", floor, e);
            throw new RuntimeException("QR ZIP generation failed");
        }
    }

    /**
     * Generate ZIP for ALL rooms of a tenant.
     */
    public byte[] generateAllRoomsQrZip(String tenantId) {
        List<Room> rooms = roomRepo.findByTenantId(tenantId);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Room room : rooms) {
                byte[] qr = generateRoomQr(tenantId, room.getRoomNo());
                zos.putNextEntry(new ZipEntry("floor" + room.getFloor() + "/room_" + room.getRoomNo() + ".png"));
                zos.write(qr);
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to create QR ZIP for all rooms", e);
            throw new RuntimeException("QR ZIP generation failed");
        }
    }

    private String buildQrContent(String tenantId, String roomNo) {
        // QR content: WhatsApp link with pre-filled room number — no secrets
        if (waPhoneNumberId != null && !waPhoneNumberId.isBlank()) {
            return "https://wa.me/" + waPhoneNumberId + "?text=Room+" + roomNo;
        }
        // Fallback: web form URL
        return "https://nivasio.in/raise-ticket?room=" + roomNo + "&t=" + tenantId;
    }

    private byte[] generateQrBytes(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE,
                    Map.of(EncodeHintType.MARGIN, 2));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("QR generation failed", e);
            throw new RuntimeException("QR code generation failed");
        }
    }
}
