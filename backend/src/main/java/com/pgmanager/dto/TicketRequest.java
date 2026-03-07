package com.pgmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank(message = "Type is required")
    private String type; // HOUSEKEEPING, LAUNDRY, MAINTENANCE, GENERAL

    @NotBlank(message = "Room number is required")
    private String roomNo;

    private String description;
    private String priority; // NORMAL, URGENT
}
