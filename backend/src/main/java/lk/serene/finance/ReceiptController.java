package lk.serene.finance;

import java.io.ByteArrayOutputStream;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class ReceiptController {
  private final JdbcTemplate db;
  private final InvoiceRepository invoices;

  public ReceiptController(JdbcTemplate db, InvoiceRepository invoices) {
    this.db = db;
    this.invoices = invoices;
  }

  @GetMapping("/{id}/receipts")
  public List<Map<String, Object>> list(@PathVariable Long id, Authentication a) {
    check(id, a);
    return db.queryForList(
        "SELECT r.ReceiptID,r.PaymentID,r.IssueDate,r.Amount,p.TransactionRef FROM Receipt r JOIN"
            + " Payment p ON r.PaymentID=p.PaymentID WHERE p.InvoiceID=?",
        id);
  }

  private Invoice check(Long id, Authentication a) {
    Access.require(
        Access.role(a, "FINANCE") || Access.role(a, "CUSTOMER"), "Finance or customer only.");
    var invoice =
        invoices
            .findById(id)
            .orElseThrow(
                () ->
                    new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND));
    Access.require(
        Access.role(a, "FINANCE") || (invoice.sent && invoice.owner.equals(a.getName())),
        "This receipt belongs to another customer.");
    return invoice;
  }

  @GetMapping(value = "/{id}/receipts/{receiptId}/pdf", produces = "application/pdf")
  public ResponseEntity<byte[]> pdf(
      @PathVariable Long id, @PathVariable Long receiptId, Authentication a) throws Exception {
    var invoice = check(id, a);
    var rows =
        db.queryForList(
            "SELECT r.ReceiptID,r.IssueDate,r.Amount,p.TransactionRef FROM Receipt r JOIN Payment p"
                + " ON r.PaymentID=p.PaymentID WHERE p.InvoiceID=? AND r.ReceiptID=?",
            id,
            receiptId);
    Access.valid(rows.size() == 1, "Receipt not found on this invoice.");
    var receipt = rows.get(0);
    var out = new ByteArrayOutputStream();
    var doc = new com.lowagie.text.Document();
    com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
    doc.open();
    for (String line :
        List.of(
            "SERENE HOTEL - PAYMENT RECEIPT",
            "Receipt: REC-" + receiptId,
            "Invoice: INV-" + id,
            "Customer: " + invoice.customerName,
            "Date: " + receipt.get("IssueDate"),
            "Received: LKR " + receipt.get("Amount"),
            "Bank reference: " + receipt.get("TransactionRef"),
            "Thank you for celebrating with Serene Hotel."))
      doc.add(new com.lowagie.text.Paragraph(line));
    doc.close();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=Serene-Receipt-" + receiptId + ".pdf")
        .body(out.toByteArray());
  }
}
