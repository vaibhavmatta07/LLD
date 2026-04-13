public class SugarDecorator extends ToppingDecorator{
    SugarDecorator(Beverage beverage) {
        super(beverage);
    }

    @Override
    public double getBaseCost() {
        return beverage.getCost() + 0.30;
    }

    @Override
    public String description() {
        return beverage.description() + ", Sugar";
    }
}
