public class Tea extends Beverage{
    Tea(Size size) {
        super(size);
    }

    @Override
    public double getBaseCost() {
        return 2.50;
    }

    @Override
    public String description() {
        return size + " Tea";
    }
}
