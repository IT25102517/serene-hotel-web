package lk.serene.marketing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lk.serene.shared.*;
import org.junit.jupiter.api.Test;

class MarketingWorkflowTest extends WebTestSupport {
  @Test
  void announcementIsManagedOnlyByMarketing() throws Exception {
    var n =
        body(
            "kind",
            "ANNOUNCEMENT",
            "title",
            "Private wedding tours available",
            "body",
            "",
            "active",
            true);
    postJson("/api/content", n, "CUSTOMER").andExpect(status().isForbidden());
    postJson("/api/content", n, "MARKETING").andExpect(status().isCreated());
    mvc.perform(get("/api/public/content"))
        .andExpect(jsonPath("$[?(@.title == 'Private wedding tours available')]").isNotEmpty());
  }

  @Test
  void draftPackagesAreNotPublic() throws Exception {
    postJson(
            "/api/marketing",
            body(
                "title",
                "Private draft package",
                "description",
                "Wedding package",
                "price",
                100000,
                "discountPercent",
                0,
                "status",
                "DRAFT",
                "imageUrl",
                "/images/venue1/1.png"),
            "MARKETING")
        .andExpect(status().isCreated());
    mvc.perform(get("/api/public/packages"))
        .andExpect(jsonPath("$[?(@.title == 'Private draft package')]").isEmpty());
  }

  @Test
  void newsletterWithoutCredentialsDoesNotClaimSuccess() throws Exception {
    postJson("/api/public/newsletter", body("email", "news@example.com", "consent", true), null)
        .andExpect(status().isServiceUnavailable());
    postJson("/api/public/newsletter", body("email", "news@example.com", "consent", false), null)
        .andExpect(status().isBadRequest());
  }
}
