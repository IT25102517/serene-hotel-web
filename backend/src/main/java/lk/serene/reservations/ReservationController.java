package lk.serene.reservations;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends CrudController<Reservation> {
  public ReservationController(ReservationRepository repo, AuditRepository audit) {
    super(repo, audit, "RESERVATIONS", true);
  }

  @Override
  protected void validate(Reservation n, Reservation old, Authentication a) {
    Access.valid(
        java.util.List.of("Grand Ballroom", "Garden Pavilion", "Lotus Hall").contains(n.hall),
        "Invalid Wedding hall");
    Access.valid(
        java.util.List.of("Rose", "Peony", "Orchid").contains(n.packageName),
        "Invalid Selected package");
    Access.valid(
        java.util.List.of("PENDING", "CONFIRMED", "CANCELLED").contains(n.status),
        "Invalid Status");
    if (old != null)
      Access.require(Access.role(a, "RESERVATIONS"), "Front office staff update reservations.");
    if (old == null && Access.role(a, "CUSTOMER"))
      Access.valid(!n.status.equals("CANCELLED"), "New reservations cannot be cancelled.");
    int capacity =
        n.hall.equals("Grand Ballroom") ? 500 : n.hall.equals("Garden Pavilion") ? 250 : 120;
    Access.valid(n.guests <= capacity, "Guest count exceeds hall capacity: " + capacity);
    n.activeSlot =
        n.status.equals("CANCELLED")
            ? "CANCELLED|" + java.util.UUID.randomUUID()
            : n.hall + "|" + n.eventDate;
  }

  @Override
  protected void beforeDelete(Reservation old) {
    Access.valid(old.status.equals("CANCELLED"), "Cancel the reservation before removing it.");
  }
}
