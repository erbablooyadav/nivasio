package in.nivasio.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
