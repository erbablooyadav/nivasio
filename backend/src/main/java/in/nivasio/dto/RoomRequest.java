package in.nivasio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    @NotBlank
    @Size(max = 10)
    private String roomNo;
    private int floor;
    @Min(1)
    private int capacity;
    private String type;
    private String propertyId;
}
