package lk.serene.finance;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class InvoiceController extends CrudController<Invoice> {
  public InvoiceController(InvoiceRepository repo, AuditRepository audit) {
    super(repo, audit, "FINANCE", true);
  }

  @Override
  protected void validate(Invoice n, Invoice old, Authentication a) {
    Access.valid(
        java.util.List.of("customer", "customer2").contains(n.customerUsername),
        "Invalid Customer login");
    Access.require(Access.role(a, "FINANCE"), "Only finance staff create or change invoices.");
    n.owner = n.customerUsername;
    if (old != null) {
      n.payments = old.payments;
      Access.valid(
          n.total.compareTo(old.getPaid()) >= 0,
          "Invoice total cannot be lower than payments received.");
    }
  }

  @Override
  protected void beforeDelete(Invoice old) {
    Access.valid(
        old.payments.isEmpty(), "Paid invoices cannot be deleted; retain their payment history.");
  }
}
