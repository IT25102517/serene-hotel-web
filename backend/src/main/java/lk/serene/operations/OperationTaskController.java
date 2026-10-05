package lk.serene.operations;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations")
public class OperationTaskController extends CrudController<OperationTask> {
  private final WeddingGuard weddings;

  public OperationTaskController(
      OperationTaskRepository repo, AuditRepository audit, WeddingGuard weddings) {
    super(repo, audit, "OPERATIONS", false);
    this.weddings = weddings;
  }

  @Override
  protected void validate(OperationTask n, OperationTask old, Authentication a) {
    var wedding = weddings.require(n.bookingReference, a);
    n.bookingReference = wedding.getReference();
    n.owner = wedding.customerEmail;
    Access.valid(
        java.util.List.of("Catering", "Venue", "Equipment", "Service").contains(n.department),
        "Invalid Department");
    Access.valid(
        java.util.List.of("Morning", "Afternoon", "Evening").contains(n.shift), "Invalid Shift");
    Access.valid(
        java.util.List.of("TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(n.status),
        "Invalid Status");
  }

  @Override
  protected void beforeDelete(OperationTask old) {
    Access.valid(
        java.util.List.of("COMPLETED", "CANCELLED").contains(old.status),
        "Only completed or cancelled tasks can be removed.");
  }
}
