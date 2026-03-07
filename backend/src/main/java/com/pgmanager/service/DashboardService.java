package com.pgmanager.service;

import com.pgmanager.dto.DashboardStats;
import com.pgmanager.model.Ticket;
import com.pgmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public DashboardStats getStats(String tenantId) {
        Instant todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant todayEnd = todayStart.plusSeconds(86400);

        long openTickets = ticketRepository.countByTenantIdAndStatus(tenantId, Ticket.Status.OPEN);
        long inProgress = ticketRepository.countByTenantIdAndStatus(tenantId, Ticket.Status.IN_PROGRESS);
        long done = ticketRepository.countByTenantIdAndStatus(tenantId, Ticket.Status.DONE);

        return DashboardStats.builder()
                .totalTickets(ticketRepository.countByTenantId(tenantId))
                .openTickets(openTickets)
                .inProgressTickets(inProgress)
                .doneTickets(done)
                .slaBreachTickets(
                        ticketRepository.findByTenantIdAndStatusAndSlaBreach(tenantId, Ticket.Status.OPEN, true).size()
                                +
                                ticketRepository
                                        .findByTenantIdAndStatusAndSlaBreach(tenantId, Ticket.Status.IN_PROGRESS, true)
                                        .size())
                .todayTickets(ticketRepository.countByTenantIdAndCreatedAtBetween(tenantId, todayStart, todayEnd))
                .totalRooms(roomRepository.countByTenantId(tenantId))
                .totalStaff(
                        userRepository.findByTenantIdAndRole(tenantId, com.pgmanager.model.AppUser.Role.STAFF).size())
                .totalResidents(
                        userRepository.findByTenantIdAndRole(tenantId, com.pgmanager.model.AppUser.Role.TENANT).size())
                .build();
    }
}
