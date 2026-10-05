package lk.serene.reservations;

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
class ReservationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired ReservationRepository repo;
  static final String PASS = "SereneDemo2026!";
  static final String BODY =
      "{\"couple\": \"Nethmi & Dilan\", \"email\": \"couple@example.com\", \"hall\": \"Grand"
          + " Ballroom\", \"eventDate\": \"2099-01-20\", \"guests\": 200, \"packageName\":"
          + " \"Rose\", \"status\": \"CONFIRMED\", \"notes\": \"Garden portraits before the"
          + " ceremony.\"}";

  @BeforeEach
  void clear() {
    repo.deleteAll();
  }

  ObjectNode create() throws Exception {
    var result =
        mvc.perform(
                post("/api/reservations")
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
        put("/api/reservations/" + r.get("id").asLong())
            .with(httpBasic(user, PASS))
            .contentType("application/json")
            .content(r.toString()));
  }

  @Test
  void authenticationAndRoleBoundary() throws Exception {
    mvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/reservations").with(httpBasic("operations", PASS)))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesRequiredFields() throws Exception {
    mvc.perform(
            post("/api/reservations")
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(
                    "{\"couple\": \"\", \"email\": \"couple@example.com\", \"hall\": \"Grand"
                        + " Ballroom\", \"eventDate\": \"2099-01-20\", \"guests\": 200,"
                        + " \"packageName\": \"Rose\", \"status\": \"CONFIRMED\", \"notes\":"
                        + " \"Garden portraits before the ceremony.\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createsUpdatesAndDeletes() throws Exception {
    var r = create();
    putRecord(r, "reservations").andExpect(status().isOk());
    r =
        (ObjectNode)
            json.readTree(
                    mvc.perform(get("/api/reservations").with(httpBasic("reservations", PASS)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get(0);
    r.put("status", "CANCELLED");
    r =
        (com.fasterxml.jackson.databind.node.ObjectNode)
            json.readTree(
                putRecord(r, "reservations")
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
    mvc.perform(
            delete("/api/reservations/" + r.get("id").asLong())
                .with(httpBasic("reservations", PASS)))
        .andExpect(status().isNoContent());
  }

  @Test
  void staleVersionIsRejected() throws Exception {
    var r = create();
    putRecord(r, "reservations").andExpect(status().isOk());
    putRecord(r, "reservations").andExpect(status().isConflict());
  }

  @Test
  void isolatesCustomerRecords() throws Exception {
    create();
    mvc.perform(get("/api/reservations").with(httpBasic("customer2", PASS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void preventsDoubleBookingAndReleasesCancelledSlot() throws Exception {
    var r = create();
    mvc.perform(
            post("/api/reservations")
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(BODY))
        .andExpect(status().isConflict());
    r.put("status", "CANCELLED");
    putRecord(r, "reservations").andExpect(status().isOk());
    mvc.perform(
            post("/api/reservations")
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(BODY))
        .andExpect(status().isCreated());
  }

  @Test
  void rejectsOversizedHallAndCustomerUpdate() throws Exception {
    var bad = json.readTree(BODY);
    ((com.fasterxml.jackson.databind.node.ObjectNode) bad).put("guests", 501);
    mvc.perform(
            post("/api/reservations")
                .with(httpBasic("customer", PASS))
                .contentType("application/json")
                .content(bad.toString()))
        .andExpect(status().isBadRequest());
    var r = create();
    putRecord(r, "customer").andExpect(status().isForbidden());
  }
}
