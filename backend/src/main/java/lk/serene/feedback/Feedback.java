package lk.serene.feedback;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "feedback_records")
public class Feedback extends OwnedRecord {
  @NotBlank
  @Size(max = 80)
  @Column(length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String customerName;

  @NotBlank public String kind;

  @NotNull
  @Min(1)
  @Max(5)
  public Integer rating;

  @NotBlank
  @Size(max = 1500)
  @Column(length = 1500)
  public String message;

  @NotBlank public String status;

  @Size(max = 1500)
  @Column(length = 1500)
  public String response;
}
