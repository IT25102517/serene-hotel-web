package lk.serene.marketing;

import java.math.*;

public final class PercentDiscount implements DiscountStrategy {
  public BigDecimal discount(BigDecimal price, BigDecimal value) {
    return price.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).min(price);
  }
}
