package lk.serene.reservations;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "reservations_records")
public class Reservation extends OwnedRecord {
  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String couple;

  @NotBlank
  @Email
  @Size(max = 160)
  @Column(length = 160)
  public String email;

  @NotBlank public String hall;
  @NotNull @FutureOrPresent public LocalDate eventDate;

  @NotNull
  @Min(20)
  @Max(500)
  public Integer guests;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String packageName;

  @NotBlank public String status;

  @Size(max = 1500)
  @Column(length = 1500)
  public String notes;

  @Column(unique = true)
  @com.fasterxml.jackson.annotation.JsonProperty(
      access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
  public String activeSlot;
}
