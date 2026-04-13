public enum Size {
    SMALL(0.9),
    MEDIUM(1.0),
    LARGE(1.3);

    private final double multiplier;

    Size(double multiplier) {
        this.multiplier = multiplier;
    }

    public double apply(double basePrice) {
        return basePrice * multiplier;
    }
}
