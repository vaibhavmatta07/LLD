class BeverageBuilder {
    private Beverage beverage;

    BeverageBuilder(Beverage beverage) {
        this.beverage = beverage;
    }

    public BeverageBuilder addMilk() {
        beverage = new MilkDecorator(beverage);
        return this;
    }

    public BeverageBuilder addSugar() {
        beverage = new SugarDecorator(beverage);
        return this;
    }

    public BeverageBuilder addWhipCream() {
        Beverage base = beverage.getBaseBeverage();

        if(base instanceof Tea) {
            throw new IllegalArgumentException("Whip Cream not allowed with Tea!");
        }
        beverage = new WhipCreamDecorator(beverage);
        return this;
    }

    public Beverage build() {
        return beverage;
    }
}
