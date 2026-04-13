import java.util.List;

public class BasicPricingStrategy implements PricingStrategy{
    @Override
    public double apply(List<Beverage> items) {
        return items.stream().mapToDouble(Beverage::getCost).sum();
    }

    @Override
    public String label() {
        return "Basic Pricing Strategy";
    }
}
