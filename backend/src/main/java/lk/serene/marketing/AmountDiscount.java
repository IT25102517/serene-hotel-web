package lk.serene.marketing;

import java.math.BigDecimal;

public final class AmountDiscount implements DiscountStrategy {
  public BigDecimal discount(BigDecimal price, BigDecimal value) {
    return value.min(price);
  }
}
