package in.nivasio.service;

import in.nivasio.exception.*;
import in.nivasio.model.RentRecord;
import in.nivasio.model.Resident;
import in.nivasio.repository.RentRecordRepository;
import in.nivasio.repository.ResidentRepository;
import in.nivasio.whatsapp.BotConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentService {

    private final RentRecordRepository rentRepo;
    private final ResidentRepository residentRepo;
    private final BotConversationService waBot;
    private final AuditService auditService;

    /**
     * Generate monthly rent records for all active residents.
     * Security: idempotent — skips if already generated for that month.
     */
    public int generateMonthlyRent(String tenantId, String month, double defaultAmount) {
        List<Resident> activeResidents = residentRepo.findByTenantIdAndActiveTrue(tenantId);
        int created = 0;

        for (Resident r : activeResidents) {
            // Idempotent: skip if already exists
            if (!rentRepo.findByTenantIdAndResidentIdAndMonth(tenantId, r.getId(), month).isEmpty()) {
                continue;
            }

            YearMonth ym = YearMonth.parse(month);
            Instant dueDate = ym.atDay(10).atStartOfDay(ZoneId.systemDefault()).toInstant();

            RentRecord rent = RentRecord.builder()
                    .tenantId(tenantId)
                    .propertyId(r.getPropertyId())
                    .residentId(r.getId())
                    .residentName(r.getName())
                    .roomNo(r.getRoomNo())
                    .amount(defaultAmount)
                    .month(month)
                    .dueDate(dueDate)
                    .status("PENDING")
                    .reminderSent(false)
                    .createdAt(Instant.now())
                    .build();
            rentRepo.save(rent);
            created++;
        }

        auditService.log(tenantId, "RENT", null, "GENERATED", null, month + " (" + created + " records)");
        log.info("Generated {} rent records for tenant {} month {}", created, tenantId, month);
        return created;
    }

    public Page<RentRecord> list(String tenantId, String month, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (month != null)
            return rentRepo.findByTenantIdAndMonth(tenantId, month, pageable);
        if (status != null)
            return rentRepo.findByTenantIdAndStatus(tenantId, status, pageable);
        return rentRepo.findByTenantId(tenantId, pageable);
    }

    public RentRecord markPaid(String tenantId, String rentId, String paymentMode,
            String transactionId, String userId) {
        RentRecord rent = rentRepo.findByIdAndTenantId(rentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent record not found"));

        rent.setStatus("PAID");
        rent.setPaidDate(Instant.now());
        rent.setPaymentMode(paymentMode);
        rent.setTransactionId(transactionId);
        rentRepo.save(rent);

        auditService.log(tenantId, "RENT", rentId, "PAID", userId, paymentMode);
        return rent;
    }

    /** Mark overdue records — called by scheduler */
    public void markOverdue() {
        List<RentRecord> overdue = rentRepo.findByStatusAndDueDateBefore("PENDING", Instant.now());
        for (RentRecord r : overdue) {
            r.setStatus("OVERDUE");
            rentRepo.save(r);
            log.info("Rent overdue: {} for resident {}", r.getId(), r.getResidentName());
        }
    }

    /** Send WhatsApp rent reminders 3 days before due date — called by scheduler */
    public void sendReminders() {
        Instant threeDaysFromNow = Instant.now().plusSeconds(3 * 24 * 3600);
        List<RentRecord> upcoming = rentRepo.findByStatusAndReminderSentFalseAndDueDateBetween(
                "PENDING", Instant.now(), threeDaysFromNow);
        for (RentRecord r : upcoming) {
            Resident resident = residentRepo.findById(r.getResidentId()).orElse(null);
            if (resident != null && resident.getPhone() != null) {
                waBot.sendMessage(resident.getPhone(),
                        "Hi " + resident.getName() + ", your rent of ₹" + r.getAmount()
                                + " for room " + r.getRoomNo() + " is due on "
                                + r.getDueDate() + ". Please pay to avoid late charges.");
                r.setReminderSent(true);
                rentRepo.save(r);
            }
        }
    }

    public long countOverdue(String tenantId) {
        return rentRepo.countByTenantIdAndStatus(tenantId, "OVERDUE");
    }
}
