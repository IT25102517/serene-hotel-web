package lk.serene.finance;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import lk.serene.shared.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FinanceWorkflowTest extends WebTestSupport {
  @Test
  void cancelledWeddingKeepsIssuedDocumentsButCannotSubmitNewTransfer() throws Exception {
    long id = invoice();
    postJson("/api/finance/" + id + "/send", body(), "FINANCE").andExpect(status().isOk());
    wedding.status = "CANCELLED";
    weddings.saveAndFlush(wedding);
    mvc.perform(get("/api/finance").with(as("CUSTOMER"))).andExpect(jsonPath("$[0].id").value(id));
    mvc.perform(get("/api/finance/" + id + "/pdf").with(as("CUSTOMER"))).andExpect(status().isOk());
    transfer(id, "100.00", "CANCELLED-TRANSFER", "CUSTOMER").andExpect(status().isBadRequest());
  }

  long invoice() throws Exception {
    var r =
        result(
            postJson(
                    "/api/finance",
                    body(
                        "bookingReference",
                        wedding.getReference(),
                        "customerName",
                        wedding.customerName,
                        "customerUsername",
                        wedding.customerEmail,
                        "total",
                        100000,
                        "dueDate",
                        wedding.eventDate.toString(),
                        "bankName",
                        "Hotel bank",
                        "accountHolder",
                        "Serene Hotel",
                        "accountNumber",
                        "123456789",
                        "bankBranch",
                        "Main branch",
                        "notes",
                        "Wedding deposit"),
                    "FINANCE")
                .andExpect(status().isCreated()));
    return r.get("id").asLong();
  }

  org.springframework.test.web.servlet.ResultActions transfer(
      long id, String amount, String reference, String role) throws Exception {
    var file =
        new MockMultipartFile(
            "proof", "receipt.pdf", "application/pdf", "%PDF-1.4\n%%EOF".getBytes());
    return mvc.perform(
        multipart("/api/finance/" + id + "/transfers")
            .file(file)
            .param("amount", amount)
            .param("reference", reference)
            .param("transferDate", LocalDate.now().toString())
            .with(csrf())
            .with(as(role)));
  }

  @Test
  void draftIsHiddenUntilSentAndPdfDownloadWorks() throws Exception {
    long id = invoice();
    mvc.perform(get("/api/finance").with(as("CUSTOMER")))
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    postJson("/api/finance/" + id + "/send", body(), "FINANCE").andExpect(status().isOk());
    mvc.perform(get("/api/finance").with(as("CUSTOMER")))
        .andExpect(jsonPath("$[0].sent").value(true));
    var r =
        mvc.perform(get("/api/finance/" + id + "/pdf").with(as("CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"))
            .andReturn();
    assertTrue(new String(r.getResponse().getContentAsByteArray(), 0, 5).equals("%PDF-"));
    mvc.perform(get("/api/finance/" + id + "/pdf").with(as("CUSTOMER2")))
        .andExpect(status().isForbidden());
  }

  @Test
  void transferOnlyChangesBalanceAfterFinanceApproval() throws Exception {
    long id = invoice();
    postJson("/api/finance/" + id + "/send", body(), "FINANCE");
    var t = result(transfer(id, "25000.00", "BANK-A", "CUSTOMER").andExpect(status().isCreated()));
    mvc.perform(get("/api/finance").with(as("CUSTOMER"))).andExpect(jsonPath("$[0].paid").value(0));
    postJson(
            "/api/finance/transfers/" + t.get("id").asLong() + "/review",
            body(
                "decision",
                "APPROVED",
                "note",
                "Verified bank credit",
                "version",
                t.get("version").asLong()),
            "FINANCE")
        .andExpect(status().isOk());
    mvc.perform(get("/api/finance").with(as("CUSTOMER")))
        .andExpect(jsonPath("$[0].paid").value(25000))
        .andExpect(jsonPath("$[0].balance").value(75000));
    transfer(id, "25000", "BANK-A", "CUSTOMER").andExpect(status().isBadRequest());
  }

  @Test
  void customerCannotApproveAndOversizedTransferIsRejected() throws Exception {
    long id = invoice();
    postJson("/api/finance/" + id + "/send", body(), "FINANCE");
    transfer(id, "100001", "BANK-B", "CUSTOMER").andExpect(status().isBadRequest());
    transfer(id, "100", "BANK-B", "CUSTOMER2").andExpect(status().isForbidden());
    var t = result(transfer(id, "100", "BANK-C", "CUSTOMER"));
    postJson(
            "/api/finance/transfers/" + t.get("id").asLong() + "/review",
            body("decision", "APPROVED", "version", t.get("version").asLong()),
            "CUSTOMER")
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectedProofCanBeResubmittedWithoutBeingMarkedPaid() throws Exception {
    long id = invoice();
    postJson("/api/finance/" + id + "/send", body(), "FINANCE");
    var t = result(transfer(id, "100", "BANK-R", "CUSTOMER"));
    postJson(
            "/api/finance/transfers/" + t.get("id").asLong() + "/review",
            body(
                "decision",
                "REJECTED",
                "note",
                "Please provide a readable receipt",
                "version",
                t.get("version").asLong()),
            "FINANCE")
        .andExpect(status().isOk());
    transfer(id, "100", "BANK-R", "CUSTOMER")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PENDING"));
    mvc.perform(get("/api/finance").with(as("CUSTOMER"))).andExpect(jsonPath("$[0].paid").value(0));
  }

  @Test
  void proofIsPrivateAndSentInvoicesAreImmutable() throws Exception {
    long id = invoice();
    postJson("/api/finance/" + id + "/send", body(), "FINANCE");
    var t = result(transfer(id, "100", "BANK-PRIVATE", "CUSTOMER"));
    mvc.perform(
            get("/api/finance/transfers/" + t.get("id").asLong() + "/proof").with(as("CUSTOMER2")))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/finance/" + id).with(csrf()).with(as("FINANCE")))
        .andExpect(status().isBadRequest());
  }
}
