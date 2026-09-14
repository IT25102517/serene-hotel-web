package lk.serene.operations;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "operations_records")
public class OperationTask extends OwnedRecord {
  @NotBlank
  @Size(max = 80)
  @Column(length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String task;

  @NotBlank public String department;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String assignee;

  @NotNull public LocalDate dueDate;
  @NotBlank public String shift;

  @NotBlank
  @Size(max = 1000)
  @Column(length = 1000)
  public String resources;

  @NotBlank public String status;
}
