package lk.serene.feedback;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("feedbackDemoSeed")
@Profile("demo")
public class DemoSeed implements CommandLineRunner {
  private final FeedbackRepository repo;

  public DemoSeed(FeedbackRepository repo) {
    this.repo = repo;
  }

  public void run(String... args) {
    if (repo.count() > 0) return;
    var r = new Feedback();
    r.bookingReference = "DEMO-WEDDING";
    r.customerName = "Nethmi";
    r.kind = "REVIEW";
    r.rating = 5;
    r.message = "The team made our planning visit feel so personal. Thank you!";
    r.status = "OPEN";
    r.response = "";
    r.owner = "customer";
    r.createdAt = java.time.Instant.now();
    r.updatedAt = r.createdAt;
    repo.save(r);
  }
}
