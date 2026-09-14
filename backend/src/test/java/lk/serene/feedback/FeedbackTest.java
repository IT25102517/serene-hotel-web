package lk.serene.feedback;

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
class FeedbackTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired FeedbackRepository repo;
  static final String PASS = "SereneDemo2026!";
  static final String BODY =
      "{\"bookingReference\": \"DEMO-WEDDING\", \"customerName\": \"Nethmi\", \"kind\": \"REVIEW\","
          + " \"rating\": 5, \"message\": \"The team made our planning visit feel so personal."
          + " Thank you!\", \"status\": \"OPEN\", \"response\": \"\"}";

  @BeforeEach
  void clear() {
    repo.deleteAll();
  }

  ObjectNode create() throws Exception {
    var result =
        mvc.perform(
                post("/api/feedback")
                    .with(httpBasic("customer", PASS))
                    .contentType("application/json")
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn();
    return (ObjectNode) json.readTree(result.getResponse().getContentAsString());
  }

  ResultActions putRecord(ObjectNode r, String user) throws Exception {
    r.remove(java.util.List.of("owner", "createdAt", "updatedAt", "paid", "balance"));
    return mvc.perform(
        put("/api/feedback/" + r.get("id").asLong())
            .with(httpBasic(user, PASS))
            .contentType("application/json")
            .content(r.toString()));
  }

  @Test
  void authenticationAndRoleBoundary() throws Exception {
    mvc.perform(get("/api/feedback")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/feedback").with(httpBasic("operations", PASS)))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesRequiredFields() throws Exception {
    mvc.perform(
            post("/api/feedback")
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(
                    "{\"bookingReference\": \"\", \"customerName\": \"Nethmi\", \"kind\":"
                        + " \"REVIEW\", \"rating\": 5, \"message\": \"The team made our planning"
                        + " visit feel so personal. Thank you!\", \"status\": \"OPEN\","
                        + " \"response\": \"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createsUpdatesAndDeletes() throws Exception {
    var r = create();
    putRecord(r, "feedback").andExpect(status().isOk());
    r =
        (ObjectNode)
            json.readTree(
                    mvc.perform(get("/api/feedback").with(httpBasic("feedback", PASS)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get(0);
    mvc.perform(delete("/api/feedback/" + r.get("id").asLong()).with(httpBasic("feedback", PASS)))
        .andExpect(status().isNoContent());
  }

  @Test
  void staleVersionIsRejected() throws Exception {
    var r = create();
    putRecord(r, "feedback").andExpect(status().isOk());
    putRecord(r, "feedback").andExpect(status().isConflict());
  }

  @Test
  void isolatesCustomerRecords() throws Exception {
    create();
    mvc.perform(get("/api/feedback").with(httpBasic("customer2", PASS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void customerCannotResolveOwnComplaint() throws Exception {
    var r = create();
    r.put("status", "RESOLVED");
    r.put("response", "fake staff response");
    putRecord(r, "customer")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.response").doesNotExist());
  }

  @Test
  void resolutionRequiresResponse() throws Exception {
    var r = create();
    r.put("status", "RESOLVED");
    r.put("response", "");
    putRecord(r, "feedback").andExpect(status().isBadRequest());
  }
}
