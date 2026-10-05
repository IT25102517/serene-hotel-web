package lk.serene.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "Payment")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_ids")
  @SequenceGenerator(name = "app_ids", sequenceName = "app_ids", allocationSize = 1)
  @Column(name = "PaymentID", columnDefinition = "int")
  public Long id;

  @Column(name = "Amount", precision = 12, scale = 2)
  public BigDecimal amount;

  @Column(name = "TransactionRef", length = 100)
  public String reference;

  @Column(name = "paid_at")
  public Instant paidAt;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "PaymentDate")
  public java.time.LocalDate paymentDate;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "PaymentMethod")
  public String method = "BANK_TRANSFER";

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(name = "PaymentStatus")
  public String status = "APPROVED";

  protected Payment() {}

  public Payment(BigDecimal amount, String reference) {
    this.amount = amount;
    this.reference = reference;
    this.paidAt = Instant.now();
    this.paymentDate = java.time.LocalDate.now();
  }
}
