package lk.serene.operations;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("operationsDemoSeed")
@Profile("demo")
public class DemoSeed implements CommandLineRunner {
  private final OperationTaskRepository repo;

  public DemoSeed(OperationTaskRepository repo) {
    this.repo = repo;
  }

  public void run(String... args) {
    if (repo.count() > 0) return;
    var r = new OperationTask();
    r.bookingReference = "DEMO-WEDDING";
    r.task = "Prepare ballroom dining layout";
    r.department = "Venue";
    r.assignee = "Venue team A";
    r.dueDate = java.time.LocalDate.now().plusDays(30);
    r.shift = "Morning";
    r.resources = "20 round tables, 200 chairs, ivory linen, floral centrepieces.";
    r.status = "IN_PROGRESS";
    r.owner = "operations";
    r.createdAt = java.time.Instant.now();
    r.updatedAt = r.createdAt;
    repo.save(r);
  }
}
