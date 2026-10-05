package lk.serene.finance;

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
class InvoiceTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired InvoiceRepository repo;
  static final String PASS = "SereneDemo2026!";
  static final String BODY =
      "{\"bookingReference\": \"DEMO-WEDDING\", \"customerName\": \"Nethmi & Dilan\","
          + " \"customerUsername\": \"customer\", \"total\": 450000.0, \"dueDate\": \"2099-01-20\","
          + " \"notes\": \"Evaluation invoice. Record verified offline deposits only.\"}";

  @BeforeEach
  void clear() {
    repo.deleteAll();
  }

  ObjectNode create() throws Exception {
    var result =
        mvc.perform(
                post("/api/finance")
                    .with(httpBasic("finance", PASS))
                    .contentType("application/json")
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn();
    return (ObjectNode) json.readTree(result.getResponse().getContentAsString());
  }

  ResultActions putRecord(ObjectNode r, String user) throws Exception {
    r.remove(java.util.List.of("owner", "createdAt", "updatedAt", "paid", "balance", "status"));
    return mvc.perform(
        put("/api/finance/" + r.get("id").asLong())
            .with(httpBasic(user, PASS))
            .contentType("application/json")
            .content(r.toString()));
  }

  @Test
  void authenticationAndRoleBoundary() throws Exception {
    mvc.perform(get("/api/finance")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/finance").with(httpBasic("operations", PASS)))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesRequiredFields() throws Exception {
    mvc.perform(
            post("/api/finance")
                .with(httpBasic("finance", PASS))
                .contentType("application/json")
                .content(
                    "{\"bookingReference\": \"\", \"customerName\": \"Nethmi & Dilan\","
                        + " \"customerUsername\": \"customer\", \"total\": 450000.0, \"dueDate\":"
                        + " \"2099-01-20\", \"notes\": \"Evaluation invoice. Record verified"
                        + " offline deposits only.\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createsUpdatesAndDeletes() throws Exception {
    var r = create();
    putRecord(r, "finance").andExpect(status().isOk());
    r =
        (ObjectNode)
            json.readTree(
                    mvc.perform(get("/api/finance").with(httpBasic("finance", PASS)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get(0);
    mvc.perform(delete("/api/finance/" + r.get("id").asLong()).with(httpBasic("finance", PASS)))
        .andExpect(status().isNoContent());
  }

  @Test
  void staleVersionIsRejected() throws Exception {
    var r = create();
    putRecord(r, "finance").andExpect(status().isOk());
    putRecord(r, "finance").andExpect(status().isConflict());
  }

  @Test
  void isolatesCustomerRecords() throws Exception {
    create();
    mvc.perform(get("/api/finance").with(httpBasic("customer2", PASS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void ledgerRejectsDuplicateAndExcessAndProtectsPaidInvoice() throws Exception {
    var r = create();
    long id = r.get("id").asLong();
    String url = "/api/finance/" + id + "/payments";
    String body = "{\"amount\":100,\"reference\":\"BANK-001\"}";
    mvc.perform(
            post(url)
                .with(httpBasic("finance", PASS))
                .contentType("application/json")
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paid").value(100))
        .andExpect(jsonPath("$.balance").value(449900));
    mvc.perform(
            post(url)
                .with(httpBasic("finance", PASS))
                .contentType("application/json")
                .content(body))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post(url)
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(body))
        .andExpect(status().isForbidden());
    mvc.perform(
            post(url)
                .with(httpBasic("finance", PASS))
                .contentType("application/json")
                .content("{\"amount\":999999,\"reference\":\"BANK-002\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(delete("/api/finance/" + id).with(httpBasic("finance", PASS)))
        .andExpect(status().isBadRequest());
  }
}
