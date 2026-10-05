package lk.serene.marketing;

import java.util.*;
import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController extends CrudController<GalleryImage> {
  public GalleryController(GalleryImageRepository repo, AuditRepository audit) {
    super(repo, audit, "MARKETING", false);
  }

  protected void validate(GalleryImage next, GalleryImage old, Authentication a) {
    Access.valid(
        List.of("slideshow", "venue1", "venue2", "venue3").contains(next.placement),
        "Choose a gallery location.");
    Access.valid(
        next.imageUrl.startsWith("https://") || next.imageUrl.startsWith("/images/"),
        "Choose an uploaded image.");
  }
}
