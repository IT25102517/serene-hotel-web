package lk.serene.shared;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String actor;
  public String module;
  public String action;
  public Long recordId;
  public Instant occurredAt;

  protected AuditEntry() {}

  public AuditEntry(String actor, String module, String action, Long id) {
    this.actor = actor;
    this.module = module;
    this.action = action;
    this.recordId = id;
    occurredAt = Instant.now();
  }
}
