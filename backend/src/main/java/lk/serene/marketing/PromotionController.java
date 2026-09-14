package lk.serene.marketing;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/marketing")
public class PromotionController extends CrudController<Promotion> {
  public PromotionController(PromotionRepository repo, AuditRepository audit) {
    super(repo, audit, "MARKETING", false);
  }

  @Override
  protected void validate(Promotion n, Promotion old, Authentication a) {
    Access.valid(
        java.util.List.of("DRAFT", "PUBLISHED", "EXPIRED").contains(n.status),
        "Invalid Publication");
  }

  @Override
  protected void beforeDelete(Promotion old) {}
}
