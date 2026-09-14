package lk.serene.feedback;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController extends CrudController<Feedback> {
  public FeedbackController(FeedbackRepository repo, AuditRepository audit) {
    super(repo, audit, "FEEDBACK", true);
  }

  @Override
  protected void validate(Feedback n, Feedback old, Authentication a) {
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
