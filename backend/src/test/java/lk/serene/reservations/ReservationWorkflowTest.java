package lk.serene.reservations;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.*;
import lk.serene.shared.*;
import org.junit.jupiter.api.Test;

@org.springframework.test.context.event.RecordApplicationEvents
class ReservationWorkflowTest extends WebTestSupport {
  @org.springframework.beans.factory.annotation.Autowired
  org.springframework.test.context.event.ApplicationEvents published;

  @Test
  void staffRepliesAndRejectionsPublishCustomerNotifications() throws Exception {
    var inquiry = inquiry("reply@example.invalid");
    long id = inquiry.get("id").asLong();
    var contacted =
        result(
            putJson(
                    "/api/inquiries/" + id + "/decision",
                    body("status", "CONTACTED", "note", "We can arrange a visit.", "version", 0),
                    "RESERVATIONS")
                .andExpect(status().isOk()));
    putJson(
            "/api/inquiries/" + id + "/decision",
            body(
                "status",
                "REJECTED",
                "note",
                "Please choose another date.",
                "version",
                contacted.get("version").asLong()),
            "RESERVATIONS")
        .andExpect(status().isOk());
    var messages =
        published.stream(lk.serene.shared.integrations.CustomerNotification.class).toList();
    assertEquals(2, messages.size());
    assertEquals("reply@example.invalid", messages.get(0).recipient());
    assertTrue(messages.get(0).message().contains("We can arrange a visit."));
    assertNotEquals(messages.get(0).key(), messages.get(1).key());
  }

  ObjectNode inquiry(String email) throws Exception {
    return result(
        postJson(
                "/api/public/inquiries",
                body(
                    "name",
                    "Asha Ravi",
                    "email",
                    email,
                    "phone",
                    "0771234567",
                    "packageType",
                    "The Garden Romance",
                    "eventDate",
                    LocalDate.now().plusDays(10).toString(),
                    "guests",
                    100,
                    "preferredVenue",
                    "Serene Garden",
                    "message",
                    "Please plan our wedding"),
                null)
            .andExpect(status().isCreated()));
  }

  ObjectNode accept(ObjectNode i, String venue) throws Exception {
    return result(
        putJson(
                "/api/inquiries/" + i.get("id").asLong() + "/decision",
                body(
                    "status",
                    "ACCEPTED",
                    "venue",
                    venue,
                    "note",
                    "Welcome to Serene",
                    "version",
                    0),
                "RESERVATIONS")
            .andExpect(status().isOk()));
  }

  @Test
  void inquiryGoesToReservationsNotMarketing() throws Exception {
    inquiry("inbox@example.com");
    mvc.perform(get("/api/inquiries").with(as("RESERVATIONS"))).andExpect(status().isOk());
    mvc.perform(get("/api/inquiries").with(as("MARKETING"))).andExpect(status().isForbidden());
  }

  @Test
  void acceptanceSharesWeddingWithDepartmentsAndBlocksDuplicate() throws Exception {
    var i = inquiry("book@example.com");
    var accepted = accept(i, "Serene Garden");
    assertTrue(accepted.get("weddingId").asLong() > 0);
    for (String role : java.util.List.of("EVENTS", "OPERATIONS", "FINANCE", "FEEDBACK"))
      mvc.perform(get("/api/weddings").with(as(role)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[?(@.customerEmail == 'book@example.com')]").isNotEmpty());
    var other = inquiry("other@example.com");
    putJson(
            "/api/inquiries/" + other.get("id").asLong() + "/decision",
            body("status", "ACCEPTED", "venue", "Serene Garden", "version", 0),
            "RESERVATIONS")
        .andExpect(status().isConflict());
  }

  @Test
  void calendarOnlyFullWhenAllThreeVenuesBooked() throws Exception {
    LocalDate date = LocalDate.now().plusDays(10);
    for (String venue : Venues.NAMES)
      accept(inquiry(java.util.UUID.randomUUID() + "@example.com"), venue);
    mvc.perform(get("/api/public/availability?month=" + YearMonth.from(date)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.days[?(@.date == '" + date + "')].fullyBooked")
                .value(org.hamcrest.Matchers.contains(true)))
        .andExpect(
            jsonPath("$.days[?(@.date == '" + date + "')].availableVenues[0]").doesNotExist());
  }

  @Test
  void anonymousInquiryRequiresPrivateCodeForAccountAccess() throws Exception {
    String email = "claim" + java.util.UUID.randomUUID() + "@example.com";
    var i = inquiry(email);
    var accepted = accept(i, "Kandyan Pavilion");
    postJson(
            "/api/public/auth/register",
            body(
                "name",
                "Asha",
                "email",
                email,
                "password",
                PASSWORD,
                "claimCode",
                i.get("claimCode").asText()),
            null)
        .andExpect(status().isCreated());
    assertEquals(
        accounts.findByEmail(email).orElseThrow().id,
        weddings.findById(accepted.get("weddingId").asLong()).orElseThrow().accountId);
  }

  @Test
  void customerCannotAcceptOrSeeOtherWeddings() throws Exception {
    var i = inquiry("notyours@example.com");
    putJson(
            "/api/inquiries/" + i.get("id").asLong() + "/decision",
            body("status", "ACCEPTED", "venue", "Serene Garden", "version", 0),
            "CUSTOMER")
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/weddings").with(as("CUSTOMER2")))
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void cancellationReleasesVenueDate() throws Exception {
    putJson(
            "/api/reservations/" + wedding.id,
            body(
                "venue",
                wedding.venue,
                "eventDate",
                wedding.eventDate.toString(),
                "guests",
                120,
                "packageName",
                wedding.packageName,
                "status",
                "CANCELLED",
                "notes",
                "Cancelled",
                "version",
                wedding.version),
            "RESERVATIONS")
        .andExpect(status().isOk());
    mvc.perform(get("/api/public/availability?month=" + YearMonth.from(wedding.eventDate)))
        .andExpect(
            jsonPath("$.days[?(@.date == '" + wedding.eventDate + "')].availableVenues[0]")
                .value(org.hamcrest.Matchers.contains("Ivory Ballroom")));
  }
}
