package lk.serene.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class OwnedRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Version public Long version;

  @Column(nullable = false)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String owner;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Instant createdAt;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Instant updatedAt;
}
