package lk.serene.operations;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lk.serene.shared.*;
import org.junit.jupiter.api.Test;

class OperationsWorkflowTest extends WebTestSupport {
  @Test
  void guestRequirementsReachOperationsAndRemainPrivate() throws Exception {
    postJson(
            "/api/guest-requirements",
            body(
                "bookingReference",
                wedding.getReference(),
                "guestCount",
                120,
                "children",
                10,
                "vegetarianMeals",
                20,
                "menuPreference",
                "Sri Lankan buffet",
                "allergies",
                "Peanuts",
                "accessibility",
                "Wheelchair access",
                "notes",
                "Two highchairs"),
            "CUSTOMER")
        .andExpect(status().isCreated());
    mvc.perform(get("/api/guest-requirements").with(as("OPERATIONS")))
        .andExpect(jsonPath("$[0].vegetarianMeals").value(20));
    mvc.perform(get("/api/guest-requirements").with(as("CUSTOMER2")))
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void mealCountCannotExceedGuests() throws Exception {
    postJson(
            "/api/guest-requirements",
            body(
                "bookingReference",
                wedding.getReference(),
                "guestCount",
                10,
                "children",
                0,
                "vegetarianMeals",
                20,
                "menuPreference",
                "Buffet"),
            "CUSTOMER")
        .andExpect(status().isBadRequest());
  }

  @Test
  void customerCannotAssignStaffAndActiveTaskCannotBeRemoved() throws Exception {
    var n =
        body(
            "bookingReference",
            wedding.getReference(),
            "task",
            "Set up dining",
            "department",
            "Venue",
            "assignee",
            "Venue team",
            "dueDate",
            wedding.eventDate.toString(),
            "shift",
            "Morning",
            "resources",
            "120 chairs",
            "status",
            "TODO");
    postJson("/api/operations", n, "CUSTOMER").andExpect(status().isForbidden());
    var r = result(postJson("/api/operations", n, "OPERATIONS").andExpect(status().isCreated()));
    mvc.perform(
            delete("/api/operations/" + r.get("id").asLong()).with(csrf()).with(as("OPERATIONS")))
        .andExpect(status().isBadRequest());
  }
}
