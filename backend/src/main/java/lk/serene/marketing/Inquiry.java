package lk.serene.marketing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lk.serene.shared.OwnedRecord;

@Entity
public class Inquiry extends OwnedRecord {
  @NotBlank
  @Size(max = 120)
  public String name;

  @NotBlank
  @Email
  @Size(max = 160)
  public String email;

  @NotBlank
  @Size(max = 1500)
  @Column(length = 1500)
  public String message;

  @NotBlank public String status;
}
