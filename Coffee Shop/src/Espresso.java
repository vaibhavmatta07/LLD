public class Espresso extends Beverage {
    Espresso(Size size) {
        super(size);
    }

    @Override
    public double getBaseCost() {
        return 3.00;
    }

    @Override
    public String description() {
        return size +  " Espresso";
    }


}
