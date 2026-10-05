package lk.serene.finance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("financeDemoSeed")
@Profile("demo")
public class DemoSeed implements CommandLineRunner {
  private final InvoiceRepository repo;

  public DemoSeed(InvoiceRepository repo) {
    this.repo = repo;
  }

  public void run(String... args) {
    if (repo.count() > 0) return;
    var r = new Invoice();
    r.bookingReference = "DEMO-WEDDING";
    r.customerName = "Nethmi & Dilan";
    r.customerUsername = "customer";
    r.total = new java.math.BigDecimal("450000");
    r.dueDate = java.time.LocalDate.now().plusDays(30);
    r.notes = "Evaluation invoice. Record verified offline deposits only.";
    r.owner = "customer";
    r.createdAt = java.time.Instant.now();
    r.updatedAt = r.createdAt;
    repo.save(r);
  }
}
