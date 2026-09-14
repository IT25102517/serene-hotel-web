package lk.serene.marketing;

import lk.serene.shared.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController extends CrudController<Inquiry> {
  public InquiryController(InquiryRepository repo, AuditRepository audit) {
    super(repo, audit, "MARKETING", true);
  }

  protected void validate(Inquiry n, Inquiry old, Authentication a) {
    if (Access.role(a, "CUSTOMER")) n.status = "NEW";
    Access.valid(
        java.util.List.of("NEW", "CONTACTED", "CLOSED").contains(n.status),
        "Invalid inquiry status");
  }
}
