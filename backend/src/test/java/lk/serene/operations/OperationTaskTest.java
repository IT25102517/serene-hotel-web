package lk.serene.operations;

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
class OperationTaskTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired OperationTaskRepository repo;
  static final String PASS = "SereneDemo2026!";
  static final String BODY =
      "{\"bookingReference\": \"DEMO-WEDDING\", \"task\": \"Prepare ballroom dining layout\","
          + " \"department\": \"Venue\", \"assignee\": \"Venue team A\", \"dueDate\":"
          + " \"2099-01-20\", \"shift\": \"Morning\", \"resources\": \"20 round tables, 200 chairs,"
          + " ivory linen, floral centrepieces.\", \"status\": \"IN_PROGRESS\"}";

  @BeforeEach
  void clear() {
    repo.deleteAll();
  }

  ObjectNode create() throws Exception {
    var result =
        mvc.perform(
                post("/api/operations")
                    .with(httpBasic("operations", PASS))
                    .contentType("application/json")
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn();
    return (ObjectNode) json.readTree(result.getResponse().getContentAsString());
  }

  ResultActions putRecord(ObjectNode r, String user) throws Exception {
    r.remove(java.util.List.of("owner", "createdAt", "updatedAt", "paid", "balance"));
    return mvc.perform(
        put("/api/operations/" + r.get("id").asLong())
            .with(httpBasic(user, PASS))
            .contentType("application/json")
            .content(r.toString()));
  }

  @Test
  void authenticationAndRoleBoundary() throws Exception {
    mvc.perform(get("/api/operations")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/operations").with(httpBasic("finance", PASS)))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesRequiredFields() throws Exception {
    mvc.perform(
            post("/api/operations")
                .with(httpBasic("operations", PASS))
                .contentType("application/json")
                .content(
                    "{\"bookingReference\": \"\", \"task\": \"Prepare ballroom dining layout\","
                        + " \"department\": \"Venue\", \"assignee\": \"Venue team A\", \"dueDate\":"
                        + " \"2099-01-20\", \"shift\": \"Morning\", \"resources\": \"20 round"
                        + " tables, 200 chairs, ivory linen, floral centrepieces.\", \"status\":"
                        + " \"IN_PROGRESS\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createsUpdatesAndDeletes() throws Exception {
    var r = create();
    putRecord(r, "operations").andExpect(status().isOk());
    r =
        (ObjectNode)
            json.readTree(
                    mvc.perform(get("/api/operations").with(httpBasic("operations", PASS)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get(0);
    r.put("status", "COMPLETED");
    r =
        (com.fasterxml.jackson.databind.node.ObjectNode)
            json.readTree(
                putRecord(r, "operations")
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
    mvc.perform(
            delete("/api/operations/" + r.get("id").asLong()).with(httpBasic("operations", PASS)))
        .andExpect(status().isNoContent());
  }

  @Test
  void staleVersionIsRejected() throws Exception {
    var r = create();
    putRecord(r, "operations").andExpect(status().isOk());
    putRecord(r, "operations").andExpect(status().isConflict());
  }

  @Test
  void protectsActiveTasks() throws Exception {
    var r = create();
    mvc.perform(
            delete("/api/operations/" + r.get("id").asLong()).with(httpBasic("operations", PASS)))
        .andExpect(status().isBadRequest());
  }
}
