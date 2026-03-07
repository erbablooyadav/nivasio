package com.pgmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long doneTickets;
    private long slaBreachTickets;
    private long todayTickets;
    private long totalRooms;
    private long totalStaff;
    private long totalResidents;
}
