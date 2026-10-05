package lk.serene.events;

import java.util.Objects;
import lk.serene.shared.*;
import org.springframework.stereotype.Service;

@Service
public class EventScheduleService {
  private final WeddingCaseRepository weddings;
  private final WeddingEventRepository activities;

  public EventScheduleService(WeddingCaseRepository weddings, WeddingEventRepository activities) {
    this.weddings = weddings;
    this.activities = activities;
  }

  public void validate(WeddingEvent next, WeddingCase wedding) {
    weddings.lock(wedding.id).orElseThrow();
    Access.valid(
        next.eventDate.equals(wedding.eventDate), "Activity date must match the wedding date.");
    Access.valid(
        next.guests <= wedding.guests, "Activity guests exceed the reservation guest count.");
    if (next.status.equals("CANCELLED")) return;
    boolean overlaps =
        activities.findByBookingReference(next.bookingReference).stream()
            .anyMatch(
                other ->
                    !Objects.equals(other.id, next.id)
                        && !other.status.equals("CANCELLED")
                        && other.eventDate.equals(next.eventDate)
                        && next.startTime.isBefore(other.endTime)
                        && next.endTime.isAfter(other.startTime));
    Access.valid(!overlaps, "This activity overlaps another activity in the wedding itinerary.");
  }
}
