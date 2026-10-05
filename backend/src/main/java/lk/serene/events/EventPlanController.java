package lk.serene.events;

import java.util.*;
import lk.serene.shared.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/plans")
public class EventPlanController {
  private final WeddingGuard guard;
  private final RelationalData relational;
  private final JdbcTemplate db;

  public EventPlanController(WeddingGuard guard, RelationalData relational, JdbcTemplate db) {
    this.guard = guard;
    this.relational = relational;
    this.db = db;
  }

  @PostMapping("/{reservationId}")
  @Transactional
  public Map<String, Integer> ensure(@PathVariable Long reservationId, Authentication a) {
    Access.require(Access.role(a, "EVENTS") || Access.role(a, "OPERATIONS"), "Planning team only.");
    guard.require("SW-" + reservationId, a);
    int id = relational.event(reservationId);
    if (Access.role(a, "EVENTS"))
      db.update(
          "UPDATE WeddingEvent SET ManagedBy=? WHERE EventID=? AND ManagedBy IS NULL",
          Access.accountId(a),
          id);
    return Map.of("eventId", id);
  }
}
