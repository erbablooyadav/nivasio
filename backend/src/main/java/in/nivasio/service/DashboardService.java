package in.nivasio.service;

import in.nivasio.dto.DashboardStats;
import in.nivasio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepo;
    private final RoomRepository roomRepo;
    private final StaffRepository staffRepo;
    private final ResidentRepository residentRepo;

    public DashboardStats getStats(String tenantId) {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        return DashboardStats.builder()
                .totalTickets(ticketRepo.countByTenantId(tenantId))
                .openTickets(ticketRepo.countByTenantIdAndStatus(tenantId, "OPEN"))
                .inProgressTickets(ticketRepo.countByTenantIdAndStatus(tenantId, "IN_PROGRESS"))
                .doneTickets(ticketRepo.countByTenantIdAndStatus(tenantId, "DONE"))
                .slaBreachTickets(ticketRepo.countByTenantIdAndSlaBreach(tenantId, true))
                .todayTickets(ticketRepo.countByTenantIdAndCreatedAtAfter(tenantId, startOfDay))
                .totalRooms(roomRepo.countByTenantId(tenantId))
                .totalStaff(staffRepo.countByTenantIdAndActiveTrue(tenantId))
                .totalResidents(residentRepo.countByTenantId(tenantId))
                .build();
    }
}
