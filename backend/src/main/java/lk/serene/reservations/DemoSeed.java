package lk.serene.reservations;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("reservationsDemoSeed")
@Profile("demo")
public class DemoSeed implements CommandLineRunner {
  private final ReservationRepository repo;

  public DemoSeed(ReservationRepository repo) {
    this.repo = repo;
  }

  public void run(String... args) {
    if (repo.count() > 0) return;
    var r = new Reservation();
    r.couple = "Nethmi & Dilan";
    r.email = "couple@example.com";
    r.hall = "Grand Ballroom";
    r.eventDate = java.time.LocalDate.now().plusDays(30);
    r.guests = 200;
    r.packageName = "Rose";
    r.status = "CONFIRMED";
    r.notes = "Garden portraits before the ceremony.";
    r.owner = "customer";
    r.createdAt = java.time.Instant.now();
    r.updatedAt = r.createdAt;
    r.activeSlot = r.hall + "|" + r.eventDate;
    repo.save(r);
  }
}
