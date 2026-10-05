package lk.serene.reservations;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
  @org.springframework.beans.factory.annotation.Autowired
  private ReservationPlanningService planning;

  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationEventPublisher notifications;

  @org.springframework.beans.factory.annotation.Autowired private RelationalData relational;
  private final WeddingCaseRepository repo;
  private final AuditRepository audit;

  public ReservationController(WeddingCaseRepository repo, AuditRepository audit) {
    this.repo = repo;
    this.audit = audit;
  }

  public record Change(
      @NotBlank String venue,
      @NotNull @FutureOrPresent LocalDate eventDate,
      @NotNull @Min(1) @Max(2000) Integer guests,
      @NotBlank @Size(max = 120) String packageName,
      @NotBlank String status,
      @Size(max = 1500) String notes,
      @NotNull Long version) {}

  @GetMapping
  public List<WeddingCase> list(Authentication a) {
    Access.require(
        Access.role(a, "RESERVATIONS") || Access.role(a, "CUSTOMER"), "Reservations team only.");
    return Access.role(a, "CUSTOMER") ? repo.findByAccountId(Access.accountId(a)) : repo.findAll();
  }

  @PutMapping("/{id}")
  @Transactional
  public WeddingCase update(@PathVariable Long id, @Valid @RequestBody Change n, Authentication a) {
    Access.require(Access.role(a, "RESERVATIONS"), "Reservations team only.");
    var w =
        repo.lock(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    if (!Objects.equals(w.version, n.version))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation changed. Refresh first.");
    Access.valid(Venues.NAMES.contains(n.venue), "Invalid venue.");
    Access.valid(List.of("CONFIRMED", "CANCELLED").contains(n.status), "Invalid status.");
    LocalDate previousDate = w.eventDate;
    String previousStatus = w.status;
    w.venue = n.venue;
    w.eventDate = n.eventDate;
    w.guests = n.guests;
    w.packageName = n.packageName;
    w.status = n.status;
    w.notes = n.notes;
    w.activeSlot = w.status.equals("CANCELLED") ? "CANCELLED|" + w.id : w.venue + "|" + w.eventDate;
    w.updatedAt = Instant.now();
    relational.reservation(w, a);
    planning.apply(w, previousDate, previousStatus);
    repo.saveAndFlush(w);
    audit.save(new AuditEntry(a.getName(), "RESERVATIONS", "UPDATE_RESERVATION", id));
    notifications.publishEvent(
        new lk.serene.shared.integrations.CustomerNotification(
            "reservation-change-" + id + "-" + w.version,
            w.customerEmail,
            "Your wedding reservation was updated",
            "Reservation "
                + w.getReference()
                + " is "
                + w.status
                + ". Date: "
                + w.eventDate
                + ". Venue: "
                + w.venue
                + ". Please review your plans in the dashboard."));
    return w;
  }
}
