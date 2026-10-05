package lk.serene.finance;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/finance")
public class FinanceWorkflowController {
  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationEventPublisher notifications;

  @org.springframework.beans.factory.annotation.Autowired private RelationalData relational;
  private final InvoiceRepository invoices;
  private final TransferRepository transfers;
  private final AuditRepository audit;
  private final WeddingGuard weddings;

  public FinanceWorkflowController(
      InvoiceRepository invoices,
      TransferRepository transfers,
      AuditRepository audit,
      WeddingGuard weddings) {
    this.invoices = invoices;
    this.transfers = transfers;
    this.audit = audit;
    this.weddings = weddings;
  }

  private Invoice invoice(Long id, Authentication a) {
    Access.require(
        Access.role(a, "FINANCE") || Access.role(a, "CUSTOMER"),
        "Finance team or customer access required.");
    var i =
        invoices
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    if (Access.role(a, "CUSTOMER")) {
      Access.require(
          i.owner.equals(a.getName()) && i.sent, "This invoice is not available to your account.");
    }
    return i;
  }

  @PostMapping("/{id}/send")
  @Transactional
  public Invoice send(@PathVariable Long id, Authentication a) {
    Access.require(Access.role(a, "FINANCE"), "Only finance can send invoices.");
    var i =
        invoices
            .lock(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    Access.valid(
        i.bankName != null
            && !i.bankName.isBlank()
            && i.accountHolder != null
            && !i.accountHolder.isBlank()
            && i.accountNumber != null
            && !i.accountNumber.isBlank(),
        "Add the hotel's bank name, account holder and account number before sending.");
    weddings.require(i.bookingReference, a);
    Access.valid(!i.sent, "This invoice has already been sent.");
    i.sent = true;
    i.documentStatus = "ISSUED";
    i.sentAt = Instant.now();
    i.updatedAt = Instant.now();
    invoices.save(i);
    audit.save(new AuditEntry(a.getName(), "FINANCE", "SEND_INVOICE", id));
    notifications.publishEvent(
        new lk.serene.shared.integrations.CustomerNotification(
            "invoice-sent-" + id,
            i.owner,
            "Your Serene Hotel invoice is ready",
            "Your invoice is ready to download in your customer dashboard. Please use the bank"
                + " details shown on the invoice and upload transfer proof after payment."));
    return i;
  }

  @GetMapping("/{id}/transfers")
  public List<TransferSubmission> submissions(@PathVariable Long id, Authentication a) {
    invoice(id, a);
    return transfers.findByInvoiceId(id);
  }

  @PostMapping(value = "/{id}/transfers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public TransferSubmission submit(
      @PathVariable Long id,
      @RequestParam BigDecimal amount,
      @RequestParam String reference,
      @RequestParam LocalDate transferDate,
      @RequestPart("proof") MultipartFile file,
      Authentication a)
      throws IOException {
    Access.require(
        Access.role(a, "CUSTOMER"), "Only the invoice customer can submit transfer evidence.");
    var i = invoice(id, a);
    weddings.require(i.bookingReference, a);
    Access.valid(
        amount != null
            && amount.signum() > 0
            && amount.scale() <= 2
            && amount.compareTo(i.getBalance()) <= 0,
        "Enter a positive amount no greater than the outstanding balance, with at most two decimal"
            + " places.");
    Access.valid(
        reference != null && !reference.isBlank() && reference.length() <= 100,
        "Enter a bank transfer reference up to 100 characters.");
    Access.valid(
        !transferDate.isAfter(LocalDate.now()), "A transfer date cannot be in the future.");
    Access.valid(
        !file.isEmpty() && file.getSize() <= 5 * 1024 * 1024,
        "Upload a PDF, PNG or JPEG file no larger than 5 MB.");
    byte[] bytes = file.getBytes();
    String mime;
    if (bytes.length >= 5
        && new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
      mime = "application/pdf";
    else if (bytes.length >= 8
        && bytes[0] == (byte) 137
        && bytes[1] == 80
        && bytes[2] == 78
        && bytes[3] == 71) mime = "image/png";
    else if (bytes.length >= 3
        && bytes[0] == (byte) 255
        && bytes[1] == (byte) 216
        && bytes[2] == (byte) 255) mime = "image/jpeg";
    else
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The file must be a valid PDF, PNG or JPEG.");
    var old = transfers.findByInvoiceIdAndReference(id, reference.trim());
    TransferSubmission t = old.orElseGet(TransferSubmission::new);
    Access.valid(
        t.id == null || t.status.equals("REJECTED"),
        "This transfer reference has already been submitted.");
    t.invoiceId = id;
    t.customerEmail = a.getName();
    t.amount = amount;
    t.reference = reference.trim();
    t.transferDate = transferDate;
    t.status = "PENDING";
    t.reviewNote = null;
    t.reviewedBy = null;
    t.reviewedAt = null;
    t.proof = bytes;
    t.contentType = mime;
    t.filename =
        "transfer-proof."
            + (mime.equals("application/pdf") ? "pdf" : mime.equals("image/png") ? "png" : "jpg");
    transfers.saveAndFlush(t);
    audit.save(new AuditEntry(a.getName(), "FINANCE", "SUBMIT_TRANSFER", id));
    return t;
  }

  public record Review(
      @NotBlank String decision, @Size(max = 1000) String note, @NotNull Long version) {}

  @PostMapping("/transfers/{id}/review")
  @Transactional
  public TransferSubmission review(
      @PathVariable Long id, @Valid @RequestBody Review r, Authentication a) {
    Access.require(Access.role(a, "FINANCE"), "Only finance can verify transfers.");
    var t =
        transfers
            .lock(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
    Access.valid(t.status.equals("PENDING"), "This transfer has already been reviewed.");
    if (!Objects.equals(t.version, r.version))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer changed. Refresh first.");
    Access.valid(List.of("APPROVED", "REJECTED").contains(r.decision), "Choose approve or reject.");
    if (r.decision.equals("REJECTED"))
      Access.valid(r.note != null && !r.note.isBlank(), "Explain why this transfer was rejected.");
    if (r.decision.equals("APPROVED")) {
      var i = invoices.lock(t.invoiceId).orElseThrow();
      Access.valid(
          t.amount.compareTo(i.getBalance()) <= 0,
          "Transfer exceeds the remaining invoice balance.");
      Access.valid(
          i.payments.stream().noneMatch(p -> p.reference.equals(t.reference)),
          "This reference is already recorded.");
      var payment = new Payment(t.amount, t.reference);
      payment.paymentDate = t.transferDate;
      i.payments.add(payment);
      i.updatedAt = Instant.now();
      var saved = invoices.saveAndFlush(i);
      var recordedPayment =
          saved.payments.stream()
              .filter(p -> p.reference.equals(t.reference))
              .findFirst()
              .orElseThrow();
      relational.receipt(recordedPayment, a);
    }
    t.status = r.decision;
    t.reviewNote = r.note;
    t.reviewedBy = a.getName();
    t.reviewedAt = Instant.now();
    transfers.save(t);
    audit.save(new AuditEntry(a.getName(), "FINANCE", r.decision + "_TRANSFER", t.invoiceId));
    var invoiceOwner = invoices.findById(t.invoiceId).orElseThrow();
    notifications.publishEvent(
        new lk.serene.shared.integrations.CustomerNotification(
            "transfer-review-" + id + "-" + t.version,
            invoiceOwner.owner,
            "Your transfer was " + r.decision.toLowerCase(java.util.Locale.ROOT),
            "Invoice INV-"
                + t.invoiceId
                + ": "
                + r.decision
                + ". "
                + Objects.toString(
                    r.note, "Please view your updated invoice and receipt in the dashboard.")));
    return t;
  }

  @GetMapping("/transfers/{id}/proof")
  public ResponseEntity<byte[]> proof(@PathVariable Long id, Authentication a) {
    var t =
        transfers
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
    invoice(t.invoiceId, a);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + t.filename + "\"")
        .header("X-Content-Type-Options", "nosniff")
        .contentType(MediaType.parseMediaType(t.contentType))
        .body(t.proof);
  }

  @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
  public ResponseEntity<byte[]> pdf(@PathVariable Long id, Authentication a) {
    var i = invoice(id, a);
    try {
      var out = new ByteArrayOutputStream();
      var doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 48, 48, 48, 48);
      com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
      doc.open();
      var heading =
          new com.lowagie.text.Font(
              com.lowagie.text.Font.HELVETICA,
              26,
              com.lowagie.text.Font.BOLD,
              new java.awt.Color(229, 103, 109));
      doc.add(new com.lowagie.text.Paragraph("SERENE HOTEL", heading));
      doc.add(new com.lowagie.text.Paragraph("WEDDING INVOICE  /  INV-" + i.id));
      doc.add(new com.lowagie.text.Paragraph(" "));
      for (String line :
          List.of(
              "Customer: " + i.customerName,
              "Email: " + i.customerUsername,
              "Wedding: " + i.bookingReference,
              "Due date: " + i.dueDate,
              "Total: LKR " + i.total.toPlainString(),
              "Paid: LKR " + i.getPaid().toPlainString(),
              "Outstanding: LKR " + i.getBalance().toPlainString(),
              "Status: " + i.getStatus())) doc.add(new com.lowagie.text.Paragraph(line));
      doc.add(new com.lowagie.text.Paragraph(" "));
      doc.add(new com.lowagie.text.Paragraph("Bank transfer instructions"));
      for (String line :
          List.of(
              "Bank: " + Objects.toString(i.bankName, ""),
              "Account holder: " + Objects.toString(i.accountHolder, ""),
              "Account number: " + Objects.toString(i.accountNumber, ""),
              "Branch: " + Objects.toString(i.bankBranch, "")))
        doc.add(new com.lowagie.text.Paragraph(line));
      doc.add(
          new com.lowagie.text.Paragraph(
              "Use INV-"
                  + i.id
                  + " as your payment description and upload your transfer proof in your Serene"
                  + " account."));
      if (i.notes != null) doc.add(new com.lowagie.text.Paragraph(i.notes));
      doc.add(new com.lowagie.text.Paragraph(" "));
      doc.add(new com.lowagie.text.Paragraph("Approved payment receipts"));
      for (var payment : i.payments)
        doc.add(
            new com.lowagie.text.Paragraph(
                payment.reference
                    + " | LKR "
                    + payment.amount.toPlainString()
                    + " | "
                    + payment.paidAt));
      doc.close();
      return ResponseEntity.ok()
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"Serene-Invoice-" + id + ".pdf\"")
          .body(out.toByteArray());
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "The invoice document could not be generated.");
    }
  }
}
