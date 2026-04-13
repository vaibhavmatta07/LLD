public class WhipCreamDecorator extends ToppingDecorator{
    WhipCreamDecorator(Beverage beverage) {
        super(beverage);
    }

    @Override
    public double getBaseCost() {
        return beverage.getCost() + 0.75;
    }

    @Override
    public String description() {
        return beverage.description() + ", Whip Cream";
    }
}
