import java.util.List;

public class HappyHourPricingStrategy implements PricingStrategy {
    @Override
    public double apply(List<Beverage> items) {
        double total = items.stream().mapToDouble(Beverage::getCost).sum();
        return total * 0.80;
    }

    @Override
    public String label() {
        return "Happy Hour (20% off)";
    }
}
