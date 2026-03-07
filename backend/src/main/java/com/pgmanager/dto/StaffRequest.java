package com.pgmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String email;

    @NotBlank(message = "Department is required")
    private String department; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, FOOD

    private String password;
}
