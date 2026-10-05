package lk.serene.marketing;

/** Simple Factory as taught in Part II: selects a concrete calculation strategy. */
public final class DiscountStrategyFactory {
  private DiscountStrategyFactory() {}

  public static DiscountStrategy create(String type) {
    return switch (type) {
      case "PERCENT" -> new PercentDiscount();
      case "AMOUNT" -> new AmountDiscount();
      default -> throw new IllegalArgumentException("Unknown discount type");
    };
  }
}
