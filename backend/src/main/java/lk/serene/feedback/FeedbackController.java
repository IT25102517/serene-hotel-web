package lk.serene.feedback;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController extends CrudController<Feedback> {
  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationEventPublisher notifications;

  @Override
  protected void afterSave(Feedback saved, Authentication a) {
    if (!Access.role(a, "CUSTOMER") && saved.response != null && !saved.response.isBlank())
      notifications.publishEvent(
          new lk.serene.shared.integrations.CustomerNotification(
              "feedback-" + saved.id + "-" + saved.version,
              saved.owner,
              "The Serene team has responded",
              saved.response));
  }

  private final WeddingGuard weddings;

  public FeedbackController(FeedbackRepository repo, AuditRepository audit, WeddingGuard weddings) {
    super(repo, audit, "FEEDBACK", true);
    this.weddings = weddings;
  }

  @Override
  protected void validate(Feedback n, Feedback old, Authentication a) {
    var wedding = weddings.require(n.bookingReference, a);
    n.bookingReference = wedding.getReference();
    n.owner = wedding.customerEmail;
    Access.valid(
        java.util.List.of("REVIEW", "COMPLAINT").contains(n.kind), "Invalid Feedback type");
    Access.valid(
        java.util.List.of("OPEN", "IN_PROGRESS", "RESOLVED").contains(n.status),
        "Invalid Resolution status");
    if (Access.role(a, "CUSTOMER")) {
      if (old != null)
        Access.require(
            old.status.equals("OPEN"), "Feedback under review can only be edited by staff.");
      n.status = "OPEN";
      n.response = old == null ? null : old.response;
    }
    if (n.status.equals("RESOLVED"))
      Access.valid(
          n.response != null && !n.response.isBlank(),
          "A resolved complaint requires a staff response.");
  }

  @Override
  protected void beforeDelete(Feedback old) {}
}
