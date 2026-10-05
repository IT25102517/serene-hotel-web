package lk.serene.marketing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lk.serene.shared.Access;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketingAnalyticsController {
  private final JdbcTemplate db;

  public MarketingAnalyticsController(JdbcTemplate db) {
    this.db = db;
  }

  @GetMapping("/api/marketing/analytics")
  public Map<String, Object> analytics(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      Authentication a) {
    Access.require(Access.role(a, "MARKETING"), "Marketing team only.");
    LocalDate start = from == null ? LocalDate.now().withDayOfYear(1) : from,
        end = to == null ? LocalDate.now().plusYears(1) : to;
    Access.valid(!end.isBefore(start), "Invalid date range.");
    String filter = " r.Status='CONFIRMED' AND r.WeddingDate BETWEEN ? AND ? ";
    Object[] args = {java.sql.Date.valueOf(start), java.sql.Date.valueOf(end)};
    var packages =
        db.queryForList(
            "SELECT p.PackageName AS label,COUNT(*) AS bookings,SUM(COALESCE(r.TotalAmount,0)) AS"
                + " quotedValue,AVG(p.Price) AS currentPackagePrice FROM Reservation r JOIN"
                + " WeddingPackage p ON r.PackageID=p.PackageID WHERE "
                + filter
                + " GROUP BY p.PackageID,p.PackageName ORDER BY bookings DESC",
            args);
    var venues =
        db.queryForList(
            "SELECT v.VenueName AS label,COUNT(*) AS bookings,AVG(v.Price) AS"
                + " currentVenuePrice,AVG(CAST(r.GuestCount AS DECIMAL(12,2))) AS"
                + " averageGuests,SUM(COALESCE(r.TotalAmount,0)) AS quotedValue FROM Reservation r"
                + " JOIN Venue v ON r.VenueID=v.VenueID WHERE "
                + filter
                + " GROUP BY v.VenueID,v.VenueName ORDER BY bookings DESC",
            args);
    var months =
        db.queryForList(
            "SELECT YEAR(r.WeddingDate) AS year,MONTH(r.WeddingDate) AS month,v.VenueName AS"
                + " venue,COUNT(*) AS bookings FROM Reservation r JOIN Venue v ON"
                + " r.VenueID=v.VenueID WHERE "
                + filter
                + " GROUP BY YEAR(r.WeddingDate),MONTH(r.WeddingDate),v.VenueName ORDER BY"
                + " year,month",
            args);
    var conversion =
        db.queryForList(
            "SELECT status AS label,COUNT(*) AS inquiries FROM reservation_inquiries WHERE"
                + " event_date BETWEEN ? AND ? GROUP BY status",
            args);
    return Map.of(
        "from",
        start,
        "to",
        end,
        "packages",
        packages,
        "venues",
        venues,
        "months",
        months,
        "inquiries",
        conversion,
        "note",
        "Bookings = currently confirmed reservations by wedding date. Quoted value is not collected"
            + " revenue; unknown quotes count as zero. Price uses today's catalogue, not historical"
            + " transaction price. Monthly patterns show association, not causation.");
  }

  @GetMapping("/api/public/offers")
  public List<Map<String, Object>> offers() {
    return db.queryForList(
        "SELECT o.OfferID,o.OfferName,o.DiscountType,o.DiscountValue,o.EndDate,op.PackageID FROM"
            + " Offer o JOIN Campaign c ON c.CampaignID=o.CampaignID JOIN OfferPackage op ON"
            + " op.OfferID=o.OfferID WHERE o.Status='ACTIVE' AND c.Status='ACTIVE' AND"
            + " CAST(GETDATE() AS DATE) BETWEEN o.StartDate AND o.EndDate AND CAST(GETDATE() AS"
            + " DATE) BETWEEN c.StartDate AND c.EndDate");
  }

  @GetMapping("/api/marketing/offer-preview")
  public Map<String, BigDecimal> preview(
      @RequestParam String type,
      @RequestParam BigDecimal price,
      @RequestParam BigDecimal value,
      Authentication a) {
    Access.require(Access.role(a, "MARKETING"), "Marketing only.");
    Access.valid(
        List.of("PERCENT", "AMOUNT").contains(type)
            && price.signum() >= 0
            && value.signum() > 0
            && (!type.equals("PERCENT") || value.compareTo(new BigDecimal("75")) <= 0),
        "Invalid discount.");
    var discount = DiscountStrategyFactory.create(type).discount(price, value);
    return Map.of("discount", discount, "finalPrice", price.subtract(discount));
  }
}
