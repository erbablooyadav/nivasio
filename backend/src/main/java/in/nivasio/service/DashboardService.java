package in.nivasio.service;

import in.nivasio.dto.DashboardStats;
import in.nivasio.model.Ticket;
import in.nivasio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepo;
    private final RoomRepository roomRepo;
    private final StaffRepository staffRepo;
    private final ResidentRepository residentRepo;

    public DashboardStats getStats(String tenantId) {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        long total = ticketRepo.countByTenantId(tenantId);
        long openTickets = ticketRepo.countByTenantIdAndStatus(tenantId, "OPEN");
        long assigned = ticketRepo.countByTenantIdAndStatus(tenantId, "ASSIGNED");
        long inProgress = ticketRepo.countByTenantIdAndStatus(tenantId, "IN_PROGRESS");
        long done = ticketRepo.countByTenantIdAndStatus(tenantId, "DONE");
        long breach = ticketRepo.countByTenantIdAndSlaBreach(tenantId, true);
        long today = ticketRepo.countByTenantIdAndCreatedAtAfter(tenantId, startOfDay);

        // SLA breach rate
        double breachRate = total > 0 ? (breach * 100.0) / total : 0;

        // Staff workload: count active (non-DONE, non-CLOSED) tickets per assigned
        // staff
        List<Ticket> activeTickets = ticketRepo.findByTenantIdAndStatusIn(
                tenantId, List.of("OPEN", "ASSIGNED", "IN_PROGRESS"));
        Map<String, Long> staffWorkload = activeTickets.stream()
                .filter(t -> t.getAssignedToName() != null)
                .collect(Collectors.groupingBy(Ticket::getAssignedToName, Collectors.counting()));

        // Department breakdown
        Map<String, Long> deptBreakdown = activeTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getDepartment, Collectors.counting()));

        // Weekly trend: tickets created per day for last 7 days
        Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 3600);
        List<Ticket> weekTickets = ticketRepo.findByTenantIdAndCreatedAtAfter(tenantId, sevenDaysAgo);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd").withZone(ZoneId.systemDefault());
        Map<String, Long> dailyMap = weekTickets.stream()
                .collect(Collectors.groupingBy(t -> fmt.format(t.getCreatedAt()), Collectors.counting()));

        List<DashboardStats.DailyTrend> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String day = fmt.format(Instant.now().minusSeconds(i * 24 * 3600L));
            weeklyTrend.add(DashboardStats.DailyTrend.builder()
                    .date(day)
                    .count(dailyMap.getOrDefault(day, 0L))
                    .build());
        }

        return DashboardStats.builder()
                .totalTickets(total)
                .openTickets(openTickets)
                .assignedTickets(assigned)
                .inProgressTickets(inProgress)
                .doneTickets(done)
                .slaBreachTickets(breach)
                .todayTickets(today)
                .totalRooms(roomRepo.countByTenantId(tenantId))
                .totalStaff(staffRepo.countByTenantIdAndActiveTrue(tenantId))
                .totalResidents(residentRepo.countByTenantId(tenantId))
                .slaBreachRate(Math.round(breachRate * 10.0) / 10.0)
                .staffWorkload(staffWorkload)
                .weeklyTrend(weeklyTrend)
                .departmentBreakdown(deptBreakdown)
                .build();
    }
}
