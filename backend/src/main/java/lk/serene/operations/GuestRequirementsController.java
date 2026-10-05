package lk.serene.operations;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest-requirements")
public class GuestRequirementsController extends CrudController<GuestRequirements> {
  private final WeddingGuard weddings;

  public GuestRequirementsController(
      GuestRequirementsRepository repo, AuditRepository audit, WeddingGuard weddings) {
    super(repo, audit, "OPERATIONS", true);
    this.weddings = weddings;
  }

  protected void validate(GuestRequirements n, GuestRequirements old, Authentication a) {
    var w = weddings.require(n.bookingReference, a);
    n.bookingReference = w.getReference();
    n.owner = w.customerEmail;
    Access.valid(
        n.children <= n.guestCount && n.vegetarianMeals <= n.guestCount,
        "Child and vegetarian meal counts must not exceed total guests.");
  }
}
