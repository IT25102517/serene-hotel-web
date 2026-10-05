package lk.serene.operations;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.WeddingLinkedRecord;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "TaskID", columnDefinition = "int"))
@Table(name = "OperationalTask")
public class OperationTask extends WeddingLinkedRecord {
  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "EventID")
  public Integer eventId;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "ManagedBy")
  public Integer managedBy;

  @NotBlank
  @Size(max = 80)
  @Column(name = "booking_reference", length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(name = "task", length = 120)
  public String task;

  @NotBlank
  @Column(name = "TaskType")
  public String department;

  @NotBlank
  @Size(max = 120)
  @Column(name = "assignee", length = 120)
  public String assignee;

  @NotNull
  @Column(name = "TaskDate")
  public LocalDate dueDate;

  @NotBlank
  @Column(name = "shift")
  public String shift;

  @NotBlank
  @Size(max = 1000)
  @Column(name = "Description", length = 1000)
  public String resources;

  @NotBlank
  @Column(name = "Status")
  public String status;

  public String weddingReference() {
    return bookingReference;
  }
}
