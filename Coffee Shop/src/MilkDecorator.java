public class MilkDecorator extends ToppingDecorator{
    MilkDecorator(Beverage beverage) {
        super(beverage);
    }

    @Override
    public double getBaseCost() {
        return beverage.getCost() + 0.60;
    }

    @Override
    public String description() {
        return beverage.description() + ", Milk";
    }
}
