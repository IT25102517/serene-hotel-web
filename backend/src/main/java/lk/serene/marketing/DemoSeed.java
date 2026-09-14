package lk.serene.marketing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("marketingDemoSeed")
@Profile("demo")
public class DemoSeed implements CommandLineRunner {
  private final PromotionRepository repo;

  public DemoSeed(PromotionRepository repo) {
    this.repo = repo;
  }

  public void run(String... args) {
    if (repo.count() > 0) return;
    var r = new Promotion();
    r.title = "The Rose Collection";
    r.description =
        "A graceful celebration with a curated buffet, floral table styling, a welcome drink, and a"
            + " dedicated wedding coordinator. Illustrative evaluation package; confirm pricing"
            + " with the hotel.";
    r.price = new java.math.BigDecimal("450000");
    r.discountPercent = 10;
    r.expiresOn = java.time.LocalDate.now().plusDays(30);
    r.status = "PUBLISHED";
    r.owner = "marketing";
    r.createdAt = java.time.Instant.now();
    r.updatedAt = r.createdAt;
    repo.save(r);
  }
}
