package lk.serene.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Embeddable
public class Payment {
  @Column(precision = 12, scale = 2)
  public BigDecimal amount;

  public String reference;
  public Instant paidAt;

  protected Payment() {}

  public Payment(BigDecimal amount, String reference) {
    this.amount = amount;
    this.reference = reference;
    this.paidAt = Instant.now();
  }
}
