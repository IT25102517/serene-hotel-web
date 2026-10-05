package lk.serene.marketing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import lk.serene.shared.OwnedRecord;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "PackageID", columnDefinition = "int"))
@Table(name = "WeddingPackage")
public class Promotion extends OwnedRecord {
  @NotBlank
  @Size(max = 120)
  @Column(name = "PackageName", length = 120)
  public String title;

  @NotBlank
  @Size(max = 1500)
  @Column(name = "Description", length = 1500)
  public String description;

  @DecimalMin("0.01")
  @Digits(integer = 10, fraction = 2)
  @Column(name = "Price")
  public BigDecimal price;

  @NotNull
  @Min(0)
  @Max(75)
  @Column(name = "discount_percent")
  public Integer discountPercent;

  @Column(name = "expires_on")
  public LocalDate expiresOn;

  @NotBlank
  @Column(name = "status")
  public String status;

  @Size(max = 300)
  @Column(name = "image_url", length = 300)
  public String imageUrl;
}
