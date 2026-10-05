package lk.serene.marketing;

import java.util.*;
import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class SiteContentController extends CrudController<SiteContent> {
  public SiteContentController(SiteContentRepository repo, AuditRepository audit) {
    super(repo, audit, "MARKETING", false);
  }

  protected void validate(SiteContent n, SiteContent old, Authentication a) {
    Access.valid(
        List.of("ANNOUNCEMENT", "STORY").contains(n.kind), "Choose announcement or story.");
    Access.valid(
        n.imageUrl == null || n.imageUrl.isBlank() || n.imageUrl.startsWith("/images/"),
        "Choose an image from the hotel image library.");
  }
}
