package lk.serene.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lk.serene.shared.*;
import org.junit.jupiter.api.Test;

class EventWorkflowTest extends WebTestSupport {
  @org.springframework.beans.factory.annotation.Autowired WeddingEventRepository events;

  private com.fasterxml.jackson.databind.node.ObjectNode activity() {
    return body(
        "bookingReference",
        wedding.getReference(),
        "activity",
        "Poruwa ceremony",
        "eventDate",
        wedding.eventDate.toString(),
        "startTime",
        "09:00",
        "endTime",
        "10:00",
        "packageName",
        wedding.packageName,
        "guests",
        120,
        "requirements",
        "Accessible front row",
        "status",
        "PLANNED");
  }

  @Test
  void customerScheduleReachesEventsTeam() throws Exception {
    postJson("/api/events", activity(), "CUSTOMER")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookingReference").value(wedding.getReference()));
    assertEquals(wedding.id, events.findAll().get(0).weddingCaseId);
    mvc.perform(get("/api/events").with(as("EVENTS")))
        .andExpect(jsonPath("$[0].activity").value("Poruwa ceremony"));
  }

  @Test
  void otherCustomerCannotCreateForThisWedding() throws Exception {
    postJson("/api/events", activity(), "CUSTOMER2").andExpect(status().isForbidden());
  }

  @Test
  void endMustFollowStart() throws Exception {
    var n = activity();
    n.put("endTime", "08:00");
    postJson("/api/events", n, "CUSTOMER").andExpect(status().isBadRequest());
  }

  @Test
  void staleEditsAreRejected() throws Exception {
    var saved = result(postJson("/api/events", activity(), "CUSTOMER"));
    var n = activity();
    n.put("version", saved.get("version").asLong());
    putJson("/api/events/" + saved.get("id").asLong(), n, "EVENTS").andExpect(status().isOk());
    putJson("/api/events/" + saved.get("id").asLong(), n, "EVENTS")
        .andExpect(status().isConflict());
  }
}
