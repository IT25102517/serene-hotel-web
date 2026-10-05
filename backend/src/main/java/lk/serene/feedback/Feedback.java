package lk.serene.feedback;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.WeddingLinkedRecord;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "FeedbackID", columnDefinition = "int"))
@AttributeOverride(
    name = "weddingCaseId",
    column = @Column(name = "ReservationID", columnDefinition = "int"))
@Table(name = "Feedback")
public class Feedback extends WeddingLinkedRecord {
  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "HandledBy")
  public Integer handledBy;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "FeedbackDate")
  public LocalDate feedbackDate;

  @NotBlank
  @Size(max = 80)
  @Column(name = "booking_reference", length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(name = "customer_name", length = 120)
  public String customerName;

  @NotBlank
  @Column(name = "kind")
  public String kind;

  @NotNull
  @Min(1)
  @Max(5)
  @Column(name = "Rating")
  public Integer rating;

  @NotBlank
  @Size(max = 1500)
  @Column(name = "Comment", length = 1500)
  public String message;

  @NotBlank
  @Column(name = "Status")
  public String status;

  @Size(max = 1500)
  @Column(name = "Response", length = 1500)
  public String response;

  public String weddingReference() {
    return bookingReference;
  }
}
