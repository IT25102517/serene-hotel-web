package lk.serene.events;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class WeddingEventController extends CrudController<WeddingEvent> {
  @org.springframework.beans.factory.annotation.Autowired private EventScheduleService scheduling;
  private final WeddingGuard weddings;

  public WeddingEventController(
      WeddingEventRepository repo, AuditRepository audit, WeddingGuard weddings) {
    super(repo, audit, "EVENTS", true);
    this.weddings = weddings;
  }

  @Override
  protected void validate(WeddingEvent n, WeddingEvent old, Authentication a) {
    var wedding = weddings.require(n.bookingReference, a);
    n.bookingReference = wedding.getReference();
    n.owner = wedding.customerEmail;
    n.packageName = wedding.packageName;
    Access.valid(
        java.util.List.of("PLANNED", "CONFIRMED", "CANCELLED").contains(n.status),
        "Invalid Status");
    Access.valid(n.endTime.isAfter(n.startTime), "End time must be after start time.");
    scheduling.validate(n, wedding);
  }

  @Override
  protected void beforeDelete(WeddingEvent old) {
    Access.valid(old.status.equals("CANCELLED"), "Cancel the activity before removing it.");
  }
}
