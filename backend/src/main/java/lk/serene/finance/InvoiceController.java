package lk.serene.finance;

import java.util.*;
import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class InvoiceController extends CrudController<Invoice> {
  private final WeddingGuard weddings;

  public InvoiceController(InvoiceRepository repo, AuditRepository audit, WeddingGuard weddings) {
    super(repo, audit, "FINANCE", true);
    this.weddings = weddings;
  }

  @Override
  public List<Invoice> list(Authentication a) {
    allowed(a);
    // Issued financial documents remain accessible after a wedding is cancelled.
    return repo.findAll().stream()
        .filter(i -> Access.role(a, "FINANCE") || (i.sent && i.owner.equals(a.getName())))
        .toList();
  }

  protected void validate(Invoice n, Invoice old, Authentication a) {
    Access.require(Access.role(a, "FINANCE"), "Only finance staff can create or edit invoices.");
    var w = weddings.require(n.bookingReference, a);
    n.bookingReference = w.getReference();
    n.customerName = w.customerName;
    n.customerUsername = w.customerEmail;
    n.owner = w.customerEmail;
    n.sent = old != null && old.sent;
    n.sentAt = old == null ? null : old.sentAt;
    if (old != null) {
      Access.valid(
          !old.sent,
          "Sent invoices cannot be edited. Create a separate invoice for additional charges.");
      n.payments = old.payments;
      Access.valid(
          n.total.compareTo(old.getPaid()) >= 0, "Total cannot be lower than payments received.");
    }
  }

  protected void beforeDelete(Invoice old) {
    Access.valid(
        !old.sent && old.payments.isEmpty(), "Only unsent, unpaid draft invoices can be removed.");
  }
}
