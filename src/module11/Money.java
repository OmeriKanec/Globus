package module11;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money {
    public enum Currency {
        RUB, USD, EUR
    }

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "amount");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currency == money.currency;
    }

    @Override
    public int hashCode() {
        BigDecimal canonicalAmount;
        try {
            canonicalAmount = amount.stripTrailingZeros();
        } catch (ArithmeticException scaleOverflow) {
            canonicalAmount = amount.setScale(Integer.MIN_VALUE);
        }
        return 31 * canonicalAmount.hashCode() + currency.hashCode();
    }
}
