package in.nivasio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    @NotBlank
    private String type;
    @NotBlank
    @Size(max = 10)
    private String roomNo;
    @Size(max = 500)
    private String description;
    private String priority;
    private String propertyId;
    @Size(max = 5, message = "Maximum 5 photos allowed")
    private List<String> photos;
}
