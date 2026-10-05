package lk.serene.reservations;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class InquiryController {
  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationEventPublisher notifications;

  @org.springframework.beans.factory.annotation.Autowired private RelationalData relational;
  private final InquiryRepository repo;
  private final WeddingCaseRepository weddings;
  private final AuditRepository audit;

  public InquiryController(
      InquiryRepository repo, WeddingCaseRepository weddings, AuditRepository audit) {
    this.repo = repo;
    this.weddings = weddings;
    this.audit = audit;
  }

  public record Input(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Email @Size(max = 160) String email,
      @NotBlank @Pattern(regexp = "[+0-9 ()-]{7,40}") String phone,
      @NotBlank @Size(max = 120) String packageType,
      @NotNull @FutureOrPresent LocalDate eventDate,
      @NotNull @Min(1) @Max(2000) Integer guests,
      @Size(max = 80) String preferredVenue,
      @NotBlank @Size(max = 1500) String message) {}

  public record Decision(
      @NotBlank String status,
      String venue,
      @Size(max = 1500) String note,
      @NotNull Long version) {}

  public record Claim(@NotBlank @Size(max = 100) String code) {}

  private static String hash(String code) {
    try {
      return java.util.HexFormat.of()
          .formatHex(
              java.security.MessageDigest.getInstance("SHA-256")
                  .digest(code.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @PostMapping("/api/public/inquiries")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public Map<String, Object> submit(@Valid @RequestBody Input r, Authentication a) {
    Access.valid(
        r.preferredVenue == null
            || r.preferredVenue.isBlank()
            || Venues.NAMES.contains(r.preferredVenue),
        "Please select a valid venue.");
    var i = new Inquiry();
    i.name = r.name;
    i.email = r.email.trim().toLowerCase(Locale.ROOT);
    i.phone = r.phone;
    i.packageType = r.packageType;
    i.eventDate = r.eventDate;
    i.guests = r.guests;
    i.preferredVenue = r.preferredVenue;
    i.message = r.message;
    String claim = UUID.randomUUID().toString() + UUID.randomUUID().toString();
    i.claimHash = hash(claim);
    if (Access.role(a, "CUSTOMER")) {
      Access.valid(i.email.equals(a.getName()), "Use your signed-in email for this inquiry.");
      i.accountId = Access.accountId(a);
    }
    repo.saveAndFlush(i);
    return Map.of(
        "id",
        i.id,
        "reference",
        "INQ-" + i.id,
        "claimCode",
        claim,
        "message",
        "Your inquiry has reached our reservations team. We will review your wedding plans"
            + " shortly.");
  }

  @GetMapping("/api/inquiries")
  public List<Inquiry> list(Authentication a) {
    Access.require(
        Access.role(a, "RESERVATIONS") || Access.role(a, "CUSTOMER"), "Reservations team only.");
    return Access.role(a, "CUSTOMER") ? repo.findByAccountId(Access.accountId(a)) : repo.findAll();
  }

  @PutMapping("/api/inquiries/{id}/decision")
  @Transactional
  public Inquiry decide(@PathVariable Long id, @Valid @RequestBody Decision d, Authentication a) {
    Access.require(
        Access.role(a, "RESERVATIONS"), "Only the reservations team can review inquiries.");
    var i =
        repo.lock(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquiry not found"));
    if (!Objects.equals(i.version, d.version))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Inquiry changed. Refresh before reviewing.");
    Access.valid(
        i.weddingId == null, "This inquiry is already accepted. Manage its reservation instead.");
    Access.valid(
        List.of("CONTACTED", "ACCEPTED", "REJECTED").contains(d.status), "Invalid decision.");
    if (d.status.equals("ACCEPTED")) {
      Access.valid(Venues.NAMES.contains(d.venue), "Choose one of the three venues.");
      Access.valid(!i.eventDate.isBefore(LocalDate.now()), "The wedding date is in the past.");
      var w = new WeddingCase();
      w.inquiryId = i.id;
      w.accountId = i.accountId;
      w.customerName = i.name;
      w.customerEmail = i.email;
      w.phone = i.phone;
      w.venue = d.venue;
      w.eventDate = i.eventDate;
      w.guests = i.guests;
      w.packageName = i.packageType;
      w.notes = i.message;
      w.activeSlot = w.venue + "|" + w.eventDate;
      relational.reservation(w, a);
      weddings.saveAndFlush(w);
      i.weddingId = w.id;
      notifications.publishEvent(
          new lk.serene.shared.integrations.CustomerNotification(
              "reservation-accepted-" + w.id,
              w.customerEmail,
              "Your Serene wedding reservation is confirmed",
              "Your wedding at "
                  + w.venue
                  + " on "
                  + w.eventDate
                  + " is confirmed. Reference: "
                  + w.getReference()
                  + ". Sign in to provide your plans."
                  + (d.note == null || d.note.isBlank()
                      ? ""
                      : "\n\nMessage from our reservations team:\n" + d.note)));
    }
    i.status = d.status;
    i.staffNote = d.note;
    i.updatedAt = Instant.now();
    repo.saveAndFlush(i);
    if (!d.status.equals("ACCEPTED")) {
      notifications.publishEvent(
          new lk.serene.shared.integrations.CustomerNotification(
              "inquiry-reply-" + i.id + "-" + i.version,
              i.email,
              "An update on your Serene wedding inquiry",
              "Hello "
                  + i.name
                  + ",\n\nOur reservations team has updated inquiry INQ-"
                  + i.id
                  + ". Status: "
                  + d.status
                  + "."
                  + (d.note == null || d.note.isBlank() ? "" : "\n\n" + d.note)));
    }
    audit.save(new AuditEntry(a.getName(), "RESERVATIONS", d.status, id));
    return i;
  }

  @DeleteMapping("/api/inquiries/{id}")
  @Transactional
  public void remove(@PathVariable Long id, Authentication a) {
    Access.require(Access.role(a, "RESERVATIONS"), "Reservations team only.");
    var i =
        repo.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquiry not found"));
    Access.valid(
        i.weddingId == null, "Accepted inquiries must remain linked to their reservations.");
    repo.delete(i);
    audit.save(new AuditEntry(a.getName(), "RESERVATIONS", "DELETE_INQUIRY", id));
  }

  @PostMapping("/api/inquiries/claim")
  @Transactional
  public Map<String, String> claim(@Valid @RequestBody Claim c, Authentication a) {
    Access.require(Access.role(a, "CUSTOMER"), "Customer account required.");
    link(Access.accountId(a), a.getName(), c.code);
    return Map.of("message", "Your inquiry is now linked to your account.");
  }

  @EventListener
  public void registered(CustomerRegistered e) {
    link(e.accountId(), e.email(), e.claimCode());
  }

  private void link(Long accountId, String email, String code) {
    var i =
        repo.findByClaimHash(hash(code))
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "The private inquiry code is invalid."));
    Access.valid(i.email.equals(email), "Use the same email address as the inquiry.");
    Access.valid(
        i.accountId == null || i.accountId.equals(accountId), "This inquiry is already linked.");
    i.accountId = accountId;
    repo.save(i);
    if (i.weddingId != null) {
      var w = weddings.findById(i.weddingId).orElseThrow();
      w.accountId = accountId;
      weddings.save(w);
    }
  }
}
