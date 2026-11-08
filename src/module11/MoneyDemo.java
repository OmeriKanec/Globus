package module11;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public final class MoneyDemo {
    private record BrokenMoney(Money money) {
        @Override
        public int hashCode() {
            return 31 * money.amount().hashCode() + money.currency().hashCode();
        }
    }

    public static void main(String[] args) {
        Money first = new Money(new BigDecimal("10.0"), Money.Currency.RUB);
        Money second = new Money(new BigDecimal("10.00"), Money.Currency.RUB);
        Money third = new Money(new BigDecimal("10.000"), Money.Currency.RUB);
        Money dollars = new Money(new BigDecimal("10.00"), Money.Currency.USD);

        BrokenMoney brokenFirst = new BrokenMoney(first);
        BrokenMoney brokenSecond = new BrokenMoney(second);
        Set<BrokenMoney> broken = new HashSet<>();
        broken.add(brokenFirst);
        broken.add(brokenSecond);
        broken.add(new BrokenMoney(third));

        Set<Money> fixed = new HashSet<>();
        fixed.add(first);
        fixed.add(second);
        fixed.add(third);

        System.out.println("Broken: equals = " + brokenFirst.equals(brokenSecond)
                + ", same hash = " + (brokenFirst.hashCode() == brokenSecond.hashCode())
                + ", set size = " + broken.size());
        System.out.println("Fixed:  equals = " + first.equals(second)
                + ", same hash = " + (first.hashCode() == second.hashCode())
                + ", set size = " + fixed.size());
        System.out.println("Different currencies: equals = " + first.equals(dollars));
    }
}
