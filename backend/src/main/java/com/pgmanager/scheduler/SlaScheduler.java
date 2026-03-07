package com.pgmanager.scheduler;

import com.pgmanager.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaScheduler {

    private final TicketService ticketService;

    /**
     * Check for SLA breaches every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void checkSlaBreaches() {
        log.debug("Running SLA breach check...");
        ticketService.checkSlaBreaches();
    }
}
