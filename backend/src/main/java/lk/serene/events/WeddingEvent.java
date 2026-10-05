package lk.serene.events;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.WeddingLinkedRecord;

@Entity
@Table(name = "EventActivity")
public class WeddingEvent extends WeddingLinkedRecord {
  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "EventID")
  public Integer eventId;

  @NotBlank
  @Size(max = 80)
  @Column(name = "booking_reference", length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(name = "activity", length = 120)
  public String activity;

  @NotNull
  @FutureOrPresent
  @Column(name = "event_date")
  public LocalDate eventDate;

  @NotNull
  @Column(name = "start_time")
  public LocalTime startTime;

  @NotNull
  @Column(name = "end_time")
  public LocalTime endTime;

  @NotBlank
  @Size(max = 120)
  @Column(name = "package_name", length = 120)
  public String packageName;

  @NotNull
  @Min(1)
  @Max(2000)
  @Column(name = "guests")
  public Integer guests;

  @Size(max = 1500)
  @Column(name = "requirements", length = 1500)
  public String requirements;

  @NotBlank
  @Column(name = "status")
  public String status;

  public String weddingReference() {
    return bookingReference;
  }
}
