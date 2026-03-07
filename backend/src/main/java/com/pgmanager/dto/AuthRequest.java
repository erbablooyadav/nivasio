package com.pgmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "Email or phone is required")
    private String identifier; // email or phone
    @NotBlank(message = "Password is required")
    private String password;
}
