package lk.serene.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "finance_records")
public class Invoice extends OwnedRecord {
  @NotBlank
  @Size(max = 80)
  @Column(length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String customerName;

  @NotBlank
  @Size(max = 80)
  @Column(length = 80)
  public String customerUsername;

  @NotNull
  @DecimalMin("0.01")
  @Digits(integer = 10, fraction = 2)
  public BigDecimal total;

  @NotNull public LocalDate dueDate;

  @Size(max = 1000)
  @Column(length = 1000)
  public String notes;

  @ElementCollection(fetch = FetchType.EAGER)
  @com.fasterxml.jackson.annotation.JsonProperty(
      access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
  public java.util.List<Payment> payments = new java.util.ArrayList<>();

  public BigDecimal getPaid() {
    return payments.stream().map(p -> p.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getBalance() {
    return total.subtract(getPaid());
  }

  public String getStatus() {
    return getBalance().signum() == 0 ? "PAID" : getPaid().signum() > 0 ? "PARTIAL" : "UNPAID";
  }
}
