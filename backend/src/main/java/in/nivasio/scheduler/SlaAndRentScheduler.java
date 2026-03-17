package in.nivasio.scheduler;

import in.nivasio.service.RentService;
import in.nivasio.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for SLA breach detection, rent overdue marking,
 * and rent reminder dispatching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlaAndRentScheduler {

    private final TicketService ticketService;
    private final RentService rentService;

    /** Check SLA breaches every 5 minutes */
    @Scheduled(fixedRateString = "${app.sla.check-interval-ms:300000}")
    public void checkSlaBreaches() {
        log.debug("Running SLA breach check...");
        ticketService.checkSlaBreaches();
    }

    /** Mark overdue rent records daily at 1:00 AM */
    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueRent() {
        log.info("Running rent overdue check...");
        rentService.markOverdue();
    }

    /** Send rent reminders daily at 10:00 AM */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendRentReminders() {
        log.info("Sending rent reminders...");
        rentService.sendReminders();
    }
}
