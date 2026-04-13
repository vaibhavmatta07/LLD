import java.util.List;

public class ComboPricingStrategy implements PricingStrategy{
    @Override
    public double apply(List<Beverage> items) {
        double total = items.stream().mapToDouble(Beverage::getCost).sum();

        boolean hasLatte = false;
        boolean hasEspresso = false;
        for (Beverage beverage: items) {
            Beverage b = beverage.getBaseBeverage();

            if (b instanceof Latte) hasLatte = true;
            if (b instanceof Espresso) hasEspresso = true;
        }

        if(hasEspresso && hasLatte) {
            total *= 0.90;
        }
        return total;
    }

    @Override
    public String label() {
        return "Combo Pricing (10% off if buying Espresso and Latte)";
    }
}
