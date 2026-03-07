package com.pgmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomRequest {
    @NotBlank(message = "Room number is required")
    private String roomNo;

    private int floor;
    private int capacity;
    private String type; // SINGLE, DOUBLE, TRIPLE, DORMITORY
}
