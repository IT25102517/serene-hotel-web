package lk.serene.marketing;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lk.serene.shared.OwnedRecord;

@Entity
@Table(name = "site_content")
public class SiteContent extends OwnedRecord {
  @NotBlank
  @Column(name = "kind")
  public String kind;

  @NotBlank
  @Size(max = 160)
  @Column(name = "title")
  public String title;

  @Size(max = 2000)
  @Column(name = "body", length = 2000)
  public String body;

  @Size(max = 300)
  @Column(name = "image_url", length = 300)
  public String imageUrl;

  @Column(name = "active")
  public boolean active = true;
}
