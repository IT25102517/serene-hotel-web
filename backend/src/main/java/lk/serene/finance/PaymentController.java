package lk.serene.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lk.serene.shared.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/finance")
public class PaymentController {
  private final InvoiceRepository repo;
  private final AuditRepository audit;

  public PaymentController(InvoiceRepository repo, AuditRepository audit) {
    this.repo = repo;
    this.audit = audit;
  }

  public record Request(
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
      @NotBlank @Size(max = 100) String reference) {}

  @PostMapping("/{id}/payments")
  @Transactional
  public Invoice pay(@PathVariable Long id, @Valid @RequestBody Request p, Authentication a) {
    Access.require(
        Access.role(a, "FINANCE"), "Only finance staff record verified offline payments.");
    Invoice i =
        repo.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    Access.valid(p.amount.compareTo(i.getBalance()) <= 0, "Payment exceeds outstanding balance.");
    Access.valid(
        i.payments.stream().noneMatch(x -> x.reference.equals(p.reference)),
        "Duplicate payment reference.");
    i.payments.add(new Payment(p.amount, p.reference));
    i.updatedAt = java.time.Instant.now();
    repo.saveAndFlush(i);
    audit.save(new AuditEntry(a.getName(), "FINANCE", "PAYMENT", id));
    return i;
  }
}
