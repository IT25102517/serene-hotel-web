package lk.serene.marketing;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DiscountStrategyTest {
  @Test
  void percentageRoundsCurrencyAndFixedNeverExceedsPrice() {
    assertEquals(
        new BigDecimal("12.35"),
        DiscountStrategyFactory.create("PERCENT")
            .discount(new BigDecimal("123.45"), new BigDecimal("10")));
    assertEquals(
        new BigDecimal("100"),
        DiscountStrategyFactory.create("AMOUNT")
            .discount(new BigDecimal("100"), new BigDecimal("200")));
  }

  @Test
  void unknownStrategyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> DiscountStrategyFactory.create("OTHER"));
  }
}
