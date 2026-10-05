package lk.serene.finance;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<TransferSubmission, Long> {
  List<TransferSubmission> findByInvoiceId(Long invoiceId);

  Optional<TransferSubmission> findByInvoiceIdAndReference(Long invoiceId, String reference);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from TransferSubmission t where t.id=:id")
  Optional<TransferSubmission> lock(@Param("id") Long id);
}
