package lk.serene.marketing;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired PromotionRepository repo;
  static final String PASS = "SereneDemo2026!";
  static final String BODY =
      "{\"title\": \"The Rose Collection\", \"description\": \"A graceful celebration with a"
          + " curated buffet, floral table styling, a welcome drink, and a dedicated wedding"
          + " coordinator. Illustrative evaluation package; confirm pricing with the hotel.\","
          + " \"price\": 450000.0, \"discountPercent\": 10, \"expiresOn\": \"2099-01-20\","
          + " \"status\": \"PUBLISHED\"}";

  @BeforeEach
  void clear() {
    repo.deleteAll();
  }

  ObjectNode create() throws Exception {
    var result =
        mvc.perform(
                post("/api/marketing")
                    .with(httpBasic("marketing", PASS))
                    .contentType("application/json")
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn();
    return (ObjectNode) json.readTree(result.getResponse().getContentAsString());
  }

  ResultActions putRecord(ObjectNode r, String user) throws Exception {
    r.remove(java.util.List.of("owner", "createdAt", "updatedAt", "paid", "balance"));
    return mvc.perform(
        put("/api/marketing/" + r.get("id").asLong())
            .with(httpBasic(user, PASS))
            .contentType("application/json")
            .content(r.toString()));
  }

  @Test
  void authenticationAndRoleBoundary() throws Exception {
    mvc.perform(get("/api/marketing")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/marketing").with(httpBasic("operations", PASS)))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesRequiredFields() throws Exception {
    mvc.perform(
            post("/api/marketing")
                .with(httpBasic("marketing", PASS))
                .contentType("application/json")
                .content(
                    "{\"title\": \"\", \"description\": \"A graceful celebration with a curated"
                        + " buffet, floral table styling, a welcome drink, and a dedicated wedding"
                        + " coordinator. Illustrative evaluation package; confirm pricing with the"
                        + " hotel.\", \"price\": 450000.0, \"discountPercent\": 10, \"expiresOn\":"
                        + " \"2099-01-20\", \"status\": \"PUBLISHED\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createsUpdatesAndDeletes() throws Exception {
    var r = create();
    putRecord(r, "marketing").andExpect(status().isOk());
    r =
        (ObjectNode)
            json.readTree(
                    mvc.perform(get("/api/marketing").with(httpBasic("marketing", PASS)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get(0);
    mvc.perform(delete("/api/marketing/" + r.get("id").asLong()).with(httpBasic("marketing", PASS)))
        .andExpect(status().isNoContent());
  }

  @Test
  void staleVersionIsRejected() throws Exception {
    var r = create();
    putRecord(r, "marketing").andExpect(status().isOk());
    putRecord(r, "marketing").andExpect(status().isConflict());
  }

  @Test
  void hidesDraftPackages() throws Exception {
    var r = create();
    r.put("status", "DRAFT");
    putRecord(r, "marketing").andExpect(status().isOk());
    mvc.perform(get("/api/public/packages"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void newsletterRequiresConsentAndReportsDemo() throws Exception {
    mvc.perform(
            post("/api/public/newsletter")
                .contentType("application/json")
                .content("{\"email\":\"guest@example.com\",\"consent\":false}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/public/newsletter")
                .contentType("application/json")
                .content("{\"email\":\"guest@example.com\",\"consent\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DEMO_NOT_SENT"));
  }

  @Test
  void customerInquiryWorkflow() throws Exception {
    String body =
        "{\"name\":\"Test guest\",\"email\":\"guest@example.com\",\"message\":\"Can we arrange"
            + " vegetarian catering?\",\"status\":\"NEW\"}";
    var result =
        mvc.perform(
                post("/api/inquiries")
                    .with(httpBasic("customer", PASS))
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    var r =
        (com.fasterxml.jackson.databind.node.ObjectNode)
            json.readTree(result.getResponse().getContentAsString());
    r.remove(java.util.List.of("owner", "createdAt", "updatedAt"));
    r.put("status", "CONTACTED");
    mvc.perform(
            put("/api/inquiries/" + r.get("id").asLong())
                .with(httpBasic("marketing", PASS))
                .contentType("application/json")
                .content(r.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONTACTED"));
  }
}
