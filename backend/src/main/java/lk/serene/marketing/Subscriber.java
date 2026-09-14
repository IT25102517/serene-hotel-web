package lk.serene.marketing;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Subscriber {
  @Id public String email;
  public String coupon;
  public String syncStatus;
  public Instant consentAt;
}
