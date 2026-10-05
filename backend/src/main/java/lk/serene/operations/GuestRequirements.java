package lk.serene.operations;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lk.serene.shared.WeddingLinkedRecord;

@Entity
@Table(name = "guest_requirements")
public class GuestRequirements extends WeddingLinkedRecord {
  @NotBlank
  @Column(name = "booking_reference", unique = true, nullable = false, length = 80)
  public String bookingReference;

  @NotNull
  @Min(1)
  @Max(2000)
  @Column(name = "guest_count")
  public Integer guestCount;

  @NotNull
  @Min(0)
  @Max(2000)
  @Column(name = "children")
  public Integer children;

  @NotNull
  @Min(0)
  @Max(2000)
  @Column(name = "vegetarian_meals")
  public Integer vegetarianMeals;

  @NotBlank
  @Size(max = 120)
  @Column(name = "menu_preference")
  public String menuPreference;

  @Size(max = 1500)
  @Column(name = "allergies", length = 1500)
  public String allergies;

  @Size(max = 1500)
  @Column(name = "accessibility", length = 1500)
  public String accessibility;

  @Size(max = 1500)
  @Column(name = "notes", length = 1500)
  public String notes;

  public String weddingReference() {
    return bookingReference;
  }
}
