package lk.serene.events;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeddingEventRepository extends JpaRepository<WeddingEvent, Long> {
  java.util.List<WeddingEvent> findByBookingReference(String bookingReference);
}
