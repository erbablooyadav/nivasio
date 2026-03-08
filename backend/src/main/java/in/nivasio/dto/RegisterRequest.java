package in.nivasio.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phone;
    @Email
    private String email;
    @NotBlank
    private String pgName;
    private String pgAddress;
    private String pgType; // PG, HOSTEL, APARTMENT, etc.
}
