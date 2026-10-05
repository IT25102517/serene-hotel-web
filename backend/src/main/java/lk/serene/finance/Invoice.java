package lk.serene.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import lk.serene.shared.WeddingLinkedRecord;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "InvoiceID", columnDefinition = "int"))
@AttributeOverride(
    name = "weddingCaseId",
    column = @Column(name = "ReservationID", columnDefinition = "int"))
@Table(name = "Invoice")
public class Invoice extends WeddingLinkedRecord {
  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "IssuedBy")
  public Integer issuedBy;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "IssueDate")
  public LocalDate issueDate;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "Status")
  public String documentStatus;

  @NotBlank
  @Size(max = 80)
  @Column(name = "booking_reference", length = 80)
  public String bookingReference;

  @NotBlank
  @Size(max = 120)
  @Column(name = "customer_name", length = 120)
  public String customerName;

  @NotBlank
  @Size(max = 160)
  @Column(name = "customer_username", length = 160)
  public String customerUsername;

  @NotNull
  @DecimalMin("0.01")
  @Digits(integer = 10, fraction = 2)
  @Column(name = "TotalAmount")
  public BigDecimal total;

  @NotNull
  @Column(name = "DueDate")
  public LocalDate dueDate;

  @Size(max = 1000)
  @Column(name = "notes", length = 1000)
  public String notes;

  @Column(name = "sent")
  public boolean sent = false;

  @Column(name = "sent_at")
  public Instant sentAt;

  @Size(max = 120)
  @Column(name = "bank_name")
  public String bankName;

  @Size(max = 120)
  @Column(name = "account_holder")
  public String accountHolder;

  @Size(max = 80)
  @Column(name = "account_number")
  public String accountNumber;

  @Size(max = 120)
  @Column(name = "bank_branch")
  public String bankBranch;

  @OneToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "InvoiceID", nullable = false, columnDefinition = "int")
  @org.hibernate.annotations.SQLRestriction("PaymentStatus = 'APPROVED'")
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

  public String weddingReference() {
    return bookingReference;
  }
}
