public class Latte extends Beverage{
    Latte(Size size) {
        super(size);
    }

    @Override
    public double getBaseCost() {
        return 4.50;
    }

    @Override
    public String description() {
        return size +  " Latte";
    }
}
