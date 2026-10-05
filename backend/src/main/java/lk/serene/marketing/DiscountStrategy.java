package lk.serene.marketing;

import java.math.BigDecimal;

public interface DiscountStrategy {
  BigDecimal discount(BigDecimal price, BigDecimal value);
}
