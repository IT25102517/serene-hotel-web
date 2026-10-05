package lk.serene.feedback;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lk.serene.shared.*;
import org.junit.jupiter.api.Test;

class FeedbackWorkflowTest extends WebTestSupport {
  private com.fasterxml.jackson.databind.node.ObjectNode feedback() {
    return body(
        "bookingReference",
        wedding.getReference(),
        "customerName",
        "Asha",
        "kind",
        "COMPLAINT",
        "rating",
        2,
        "message",
        "Please clarify the menu options",
        "status",
        "OPEN",
        "response",
        "");
  }

  @Test
  void customerCannotSetStaffResolution() throws Exception {
    var n = feedback();
    n.put("status", "RESOLVED");
    n.put("response", "Forged reply");
    postJson("/api/feedback", n, "CUSTOMER")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.response").doesNotExist());
  }

  @Test
  void resolutionNeedsReplyAndCustomerCannotEditAfterReview() throws Exception {
    var r = result(postJson("/api/feedback", feedback(), "CUSTOMER"));
    var n = feedback();
    n.put("version", r.get("version").asLong());
    n.put("status", "RESOLVED");
    putJson("/api/feedback/" + r.get("id").asLong(), n, "FEEDBACK")
        .andExpect(status().isBadRequest());
    n.put("response", "We have shared vegetarian menu options.");
    var updated =
        result(
            putJson("/api/feedback/" + r.get("id").asLong(), n, "FEEDBACK")
                .andExpect(status().isOk()));
    n.put("version", updated.get("version").asLong());
    putJson("/api/feedback/" + r.get("id").asLong(), n, "CUSTOMER")
        .andExpect(status().isForbidden());
  }

  @Test
  void anotherCustomerCannotSubmitAgainstWedding() throws Exception {
    postJson("/api/feedback", feedback(), "CUSTOMER2").andExpect(status().isForbidden());
  }
}
