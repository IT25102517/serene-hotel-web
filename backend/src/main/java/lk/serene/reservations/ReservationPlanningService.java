package lk.serene.reservations;

import java.time.LocalDate;
import lk.serene.shared.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Coordinates dependent plans inside the reservation controller's transaction. */
@Service
public class ReservationPlanningService {
  private final JdbcTemplate db;
  private final boolean enabled;

  public ReservationPlanningService(
      JdbcTemplate db, @Value("${serene.relational-schema:false}") boolean enabled) {
    this.db = db;
    this.enabled = enabled;
  }

  public void apply(WeddingCase wedding, LocalDate previousDate, String previousStatus) {
    if (!enabled) return;
    int excessive =
        db.queryForObject(
            "SELECT COUNT(*) FROM CateringRequirement c JOIN WeddingEvent e ON c.EventID=e.EventID"
                + " WHERE e.ReservationID=? AND c.GuestCount>?",
            Integer.class,
            wedding.id,
            wedding.guests);
    Access.valid(
        excessive == 0,
        "Update catering guest counts before reducing the reservation guest count.");
    if (!wedding.eventDate.equals(previousDate)) {
      long days = java.time.temporal.ChronoUnit.DAYS.between(previousDate, wedding.eventDate);
      int duties =
          db.queryForObject(
              "SELECT COUNT(*) FROM StaffTaskAssignment a JOIN OperationalTask t ON"
                  + " a.TaskID=t.TaskID WHERE t.wedding_case_id=?",
              Integer.class,
              wedding.id);
      Access.valid(
          duties == 0,
          "Remove or reassign existing staff duties before changing the wedding date to avoid"
              + " scheduling conflicts.");
      db.update(
          "UPDATE WeddingEvent SET EventDate=? WHERE ReservationID=?",
          wedding.eventDate,
          wedding.id);
      db.update(
          "UPDATE EventActivity SET event_date=?,version=version+1 WHERE wedding_case_id=?",
          wedding.eventDate,
          wedding.id);
      db.update(
          "UPDATE OperationalTask SET TaskDate=DATEADD(day,?,TaskDate),version=version+1 WHERE"
              + " wedding_case_id=? AND Status NOT IN ('COMPLETED','CANCELLED')",
          days,
          wedding.id);
    }
    db.update(
        "UPDATE EventActivity SET package_name=?,version=version+1 WHERE wedding_case_id=? AND"
            + " package_name<>?",
        wedding.packageName,
        wedding.id,
        wedding.packageName);
    if (wedding.status.equals("CANCELLED") && !previousStatus.equals("CANCELLED")) {
      db.update("UPDATE WeddingEvent SET Status='CANCELLED' WHERE ReservationID=?", wedding.id);
      db.update(
          "UPDATE EventActivity SET status='CANCELLED',version=version+1 WHERE wedding_case_id=?",
          wedding.id);
      db.update(
          "UPDATE OperationalTask SET Status='CANCELLED',version=version+1 WHERE wedding_case_id=?"
              + " AND Status<>'COMPLETED'",
          wedding.id);
      db.update(
          "DELETE FROM StaffTaskAssignment WHERE TaskID IN (SELECT TaskID FROM OperationalTask"
              + " WHERE wedding_case_id=? AND Status='CANCELLED')",
          wedding.id);
    }
    if (wedding.status.equals("CONFIRMED") && previousStatus.equals("CANCELLED"))
      db.update("UPDATE WeddingEvent SET Status='PLANNED' WHERE ReservationID=?", wedding.id);
  }
}
