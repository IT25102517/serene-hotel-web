package lk.serene.reservations;

import java.time.LocalDate;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AvailabilityController {
  private final ReservationRepository repo;

  public AvailabilityController(ReservationRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/public/availability")
  public List<Map<String, Object>> availability(@RequestParam LocalDate date) {
    return List.of("Grand Ballroom", "Garden Pavilion", "Lotus Hall").stream()
        .map(
            h -> {
              boolean available =
                  repo.findAll().stream()
                      .noneMatch(
                          r ->
                              h.equals(r.hall)
                                  && date.equals(r.eventDate)
                                  && !"CANCELLED".equals(r.status));
              return Map.<String, Object>of(
                  "hall",
                  h,
                  "capacity",
                  h.equals("Grand Ballroom") ? 500 : h.equals("Garden Pavilion") ? 250 : 120,
                  "available",
                  available);
            })
        .toList();
  }
}
