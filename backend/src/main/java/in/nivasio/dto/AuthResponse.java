package in.nivasio.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String userId;
    private String tenantId;
    private String name;
    private String role;
    private String propertyId;
    // refreshToken sent via HttpOnly cookie — not in response body
}
