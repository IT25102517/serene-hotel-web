package lk.serene.marketing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "marketing_records")
public class Promotion extends OwnedRecord {
  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String title;

  @NotBlank
  @Size(max = 1500)
  @Column(length = 1500)
  public String description;

  @NotNull
  @DecimalMin("0.01")
  @Digits(integer = 10, fraction = 2)
  public BigDecimal price;

  @NotNull
  @Min(0)
  @Max(75)
  public Integer discountPercent;

  @NotNull @FutureOrPresent public LocalDate expiresOn;
  @NotBlank public String status;
}
