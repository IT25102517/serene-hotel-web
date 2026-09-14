package lk.serene.marketing;

import java.time.LocalDate;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicMarketingController {
  private final PromotionRepository repo;

  public PublicMarketingController(PromotionRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/public/packages")
  public List<Promotion> packages() {
    return repo.findAll().stream()
        .filter(p -> p.status.equals("PUBLISHED") && !p.expiresOn.isBefore(LocalDate.now()))
        .toList();
  }
}
