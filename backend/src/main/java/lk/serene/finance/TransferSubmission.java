package lk.serene.finance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(
    name = "transfer_submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"invoice_id", "reference"}))
public class TransferSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Long id;

  @Version
  @Column(name = "version")
  public Long version;

  @Column(name = "invoice_id", nullable = false, columnDefinition = "int")
  public Long invoiceId;

  @Column(name = "customer_email", nullable = false, length = 160)
  public String customerEmail;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  public BigDecimal amount;

  @Column(name = "reference", nullable = false, length = 100)
  public String reference;

  @Column(name = "transfer_date", nullable = false)
  public LocalDate transferDate;

  @Column(name = "status", nullable = false, length = 24)
  public String status = "PENDING";

  @Column(name = "review_note", length = 1000)
  public String reviewNote;

  @Column(name = "reviewed_by", length = 160)
  public String reviewedBy;

  @Column(name = "reviewed_at")
  public Instant reviewedAt;

  @Column(name = "created_at")
  public Instant createdAt = Instant.now();

  @Column(name = "filename", nullable = false, length = 150)
  public String filename;

  @Column(name = "content_type", nullable = false, length = 40)
  public String contentType;

  @Lob
  @JsonIgnore
  @Column(name = "proof", nullable = false)
  public byte[] proof;
}
