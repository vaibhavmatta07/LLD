import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<Beverage> items = new ArrayList<>();
    private PricingStrategy pricingStrategy;

    Order(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public void addItem(Beverage beverage) {
        items.add(beverage);
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public double subtotal() {
        return items.stream().mapToDouble(Beverage::getCost).sum();
    }

    public double total() {
        double totalCost = pricingStrategy.apply(items);
        return totalCost;
    }
}
