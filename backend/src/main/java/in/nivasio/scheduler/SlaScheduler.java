package in.nivasio.scheduler;

import in.nivasio.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaScheduler {

    private final TicketService ticketService;

    @Scheduled(fixedRateString = "${app.sla.check-interval-ms:300000}") // 5 minutes
    public void checkSlaBreach() {
        log.debug("Running SLA breach check...");
        ticketService.checkSlaBreaches();
    }
}
