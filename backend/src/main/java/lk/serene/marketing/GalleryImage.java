package lk.serene.marketing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "gallery_images")
public class GalleryImage extends OwnedRecord {
  @NotBlank
  @Size(max = 120)
  @Column(length = 120)
  public String title;

  @NotBlank
  @Size(max = 500)
  @Column(name = "image_url", length = 500)
  public String imageUrl;

  @NotBlank
  @Column(length = 30)
  public String placement;

  public boolean active = true;
}
