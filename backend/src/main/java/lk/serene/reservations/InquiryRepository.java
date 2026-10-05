package lk.serene.reservations;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
  Optional<Inquiry> findByClaimHash(String hash);

  List<Inquiry> findByAccountId(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Inquiry i where i.id=:id")
  Optional<Inquiry> lock(@Param("id") Long id);
}
