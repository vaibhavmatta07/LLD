//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        Beverage espresso = new Espresso(Size.SMALL);

        Beverage latte = new BeverageBuilder(new Latte(Size.LARGE))
                .addMilk()
                .addSugar()
                .build();

        Beverage tea = new BeverageBuilder(new Tea(Size.MEDIUM))
                .addSugar()
                //.addWhipCream()
                .build();

        System.out.println(espresso.description() + " -> $" + espresso.getCost());
        System.out.println(latte.description() + " -> $" + latte.getCost());
        System.out.println(tea.description() + " -> $" + tea.getCost());

        Order order = new Order(new HappyHourPricingStrategy());

        order.addItem(espresso);
        order.addItem(latte);
        order.addItem(tea);

        System.out.println("\nSubtotal: $" + order.subtotal());
        System.out.println("\nTotal Price after discount: $" + order.total());

        order.setPricingStrategy(new LoyaltyPricingStrategy());
        System.out.println("\nLoyalty Total: $" + order.total());

        order.setPricingStrategy(new ComboPricingStrategy());
        System.out.println("\nCombo Total: $" + order.total());
    }
}