package in.nivasio.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long doneTickets;
    private long slaBreachTickets;
    private long todayTickets;
    private long totalRooms;
    private long totalStaff;
    private long totalResidents;

    // Phase 2A enhancements
    private double slaBreachRate; // % of tickets that breached SLA
    private Map<String, Long> staffWorkload; // staffName -> active ticket count
    private List<DailyTrend> weeklyTrend; // last 7 days ticket count
    private Map<String, Long> departmentBreakdown; // department -> ticket count

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private long count;
    }
}
