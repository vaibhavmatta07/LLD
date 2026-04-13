public abstract class Beverage {
    protected Size size;

    Beverage(Size size) {
        this.size = size;
    }

    public abstract double getBaseCost();

    public double getCost() {
        return size.apply(getBaseCost());
    }

    public abstract String description();

    public Beverage getBaseBeverage() {
        return this;
    }
}
