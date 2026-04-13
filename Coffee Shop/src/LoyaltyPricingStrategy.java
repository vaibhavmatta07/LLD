import java.util.List;

public class LoyaltyPricingStrategy implements PricingStrategy{
    @Override
    public double apply(List<Beverage> items) {
        double total = items.stream().mapToDouble(Beverage::getCost).sum();
        return total * 0.85;
    }

    @Override
    public String label() {
        return "Loyalty Pricing (15% off)";
    }
}
