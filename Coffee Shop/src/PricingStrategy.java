import java.util.List;

interface PricingStrategy {
    double apply(List<Beverage> items);
    String label();
}
