package lk.serene.reservations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "reservation_inquiries")
public class Inquiry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Long id;

  @Version
  @Column(name = "version")
  public Long version;

  @Column(name = "name", nullable = false, length = 120)
  public String name;

  @Column(name = "email", nullable = false, length = 160)
  public String email;

  @Column(name = "phone", nullable = false, length = 40)
  public String phone;

  @Column(name = "package_type", nullable = false, length = 120)
  public String packageType;

  @Column(name = "event_date", nullable = false)
  public LocalDate eventDate;

  @Column(name = "guests", nullable = false)
  public Integer guests;

  @Column(name = "preferred_venue", length = 80)
  public String preferredVenue;

  @Column(name = "message", nullable = false, length = 1500)
  public String message;

  @Column(name = "status", nullable = false, length = 24)
  public String status = "NEW";

  @Column(name = "staff_note", length = 1500)
  public String staffNote;

  @Column(name = "account_id", columnDefinition = "int")
  public Long accountId;

  @Column(name = "wedding_id", columnDefinition = "int")
  public Long weddingId;

  @JsonIgnore
  @Column(name = "claim_hash", unique = true, length = 64)
  public String claimHash;

  @Column(name = "created_at")
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  public Instant updatedAt = Instant.now();
}
