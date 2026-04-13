abstract class ToppingDecorator extends Beverage{
    protected Beverage beverage;

    ToppingDecorator(Beverage beverage) {
        super(beverage.size);
        this.beverage = beverage;
    }

    @Override
    public Beverage getBaseBeverage() {
        return beverage.getBaseBeverage();
    }
}
