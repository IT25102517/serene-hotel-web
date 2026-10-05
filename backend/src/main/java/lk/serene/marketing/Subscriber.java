package lk.serene.marketing;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "subscriber")
public class Subscriber {
  @Id
  @Column(name = "email")
  public String email;

  @Column(name = "coupon")
  public String coupon;

  @Column(name = "sync_status")
  public String syncStatus;

  @Column(name = "consent_at")
  public Instant consentAt;
}
