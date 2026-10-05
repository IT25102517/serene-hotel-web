package lk.serene.reservations;

import java.time.*;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AvailabilityController {
  private final WeddingCaseRepository repo;

  public AvailabilityController(WeddingCaseRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/public/availability")
  public Map<String, Object> month(@RequestParam String month) {
    YearMonth ym;
    try {
      ym = YearMonth.parse(month);
    } catch (Exception e) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Use a month in YYYY-MM format.");
    }
    Access.valid(ym.getYear() >= 2020 && ym.getYear() <= 2100, "Invalid calendar year.");
    var bookings =
        repo.findByEventDateBetweenAndStatus(ym.atDay(1), ym.atEndOfMonth(), "CONFIRMED");
    List<Map<String, Object>> days = new ArrayList<>();
    for (int d = 1; d <= ym.lengthOfMonth(); d++) {
      LocalDate date = ym.atDay(d);
      var occupied =
          bookings.stream().filter(w -> w.eventDate.equals(date)).map(w -> w.venue).toList();
      var available = Venues.NAMES.stream().filter(v -> !occupied.contains(v)).toList();
      days.add(
          Map.of(
              "date",
              date.toString(),
              "availableVenues",
              available,
              "fullyBooked",
              available.isEmpty()));
    }
    return Map.of("month", month, "days", days);
  }
}
