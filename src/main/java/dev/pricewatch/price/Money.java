package dev.pricewatch.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * A price. Never a double: binary floating point cannot represent 0.10 exactly,
 * so threshold comparisons would drift.
 *
 * <p>The amount is normalised to a fixed scale on construction so that two equal
 * prices are also {@code equals}. Comparisons still go through {@link #compareTo},
 * because {@code BigDecimal.equals} treats 1.0 and 1.00 as different.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    /** Matches NUMERIC(19,4) in the schema. */
    public static final int SCALE = 4;

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("price cannot be negative: " + amount);
        }
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public boolean isAtOrBelow(Money threshold) {
        return compareTo(threshold) <= 0;
    }

    @Override
    public int compareTo(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return amount.stripTrailingZeros().toPlainString() + " " + currency.getCurrencyCode();
    }
}
