package lk.serene.marketing;

import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@org.springframework.context.annotation.Profile("!portable")
public class InitialContent implements CommandLineRunner {
  private final PromotionRepository packages;
  private final SiteContentRepository content;

  public InitialContent(PromotionRepository packages, SiteContentRepository content) {
    this.packages = packages;
    this.content = content;
  }

  public void run(String... args) {
    if (packages.count() == 0) {
      String[] titles = {"The Ivory Celebration", "The Kandyan Heritage", "The Garden Romance"};
      String[] descriptions = {
        "An elegant western-style celebration in Ivory Ballroom. Refined dining, a beautifully"
            + " styled reception and thoughtful wedding coordination, tailored around your story.",
        "Celebrate tradition in Kandyan Pavilion. A graceful setting for your poruwa ceremony, warm"
            + " Sri Lankan hospitality and a celebration that honours the details you love.",
        "An open-air wedding at Serene Garden. Exchange vows with a sea breeze, gather beneath the"
            + " palms and celebrate with dining and styling shaped around you."
      };
      for (int i = 0; i < 3; i++) {
        var p = new Promotion();
        p.title = titles[i];
        p.description = descriptions[i];
        p.price = null;
        p.discountPercent = 0;
        p.status = "PUBLISHED";
        p.imageUrl = "/images/venue" + (i + 1) + "/1.png";
        p.owner = "sereneadmin@serene.com";
        p.createdAt = Instant.now();
        p.updatedAt = p.createdAt;
        packages.save(p);
      }
    }
    if (content.count() == 0) {
      var c = new SiteContent();
      c.kind = "ANNOUNCEMENT";
      c.title = "Your forever begins at Serene. Speak to our team about your wedding day.";
      c.body = "";
      c.owner = "sereneadmin@serene.com";
      c.createdAt = Instant.now();
      c.updatedAt = c.createdAt;
      content.save(c);
      var story = new SiteContent();
      story.kind = "STORY";
      story.title = "Every detail, beautifully yours.";
      story.body =
          "From your first visit to your final dance, our wedding team brings your ideas together"
              + " with care. Discover spaces for intimate traditions, grand celebrations and"
              + " everything in between.";
      story.imageUrl = "/images/slideshow/4.png";
      story.owner = c.owner;
      story.createdAt = c.createdAt;
      story.updatedAt = c.createdAt;
      content.save(story);
    }
  }
}
