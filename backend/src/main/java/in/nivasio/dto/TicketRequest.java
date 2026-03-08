package in.nivasio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

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
}
