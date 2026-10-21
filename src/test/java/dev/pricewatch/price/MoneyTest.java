package dev.pricewatch.price;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void differentScalesAreEqual() {
        assertThat(Money.of("49.9", "GBP")).isEqualTo(Money.of("49.9000", "GBP"));
    }

    @Test
    void thresholdIsInclusive() {
        Money threshold = Money.of("49.99", "GBP");
        assertThat(Money.of("49.99", "GBP").isAtOrBelow(threshold)).isTrue();
        assertThat(Money.of("50.00", "GBP").isAtOrBelow(threshold)).isFalse();
    }

    @Test
    void rejectsNegativeAndOverPrecision() {
        assertThatThrownBy(() -> Money.of("-1", "GBP")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.of("1.00001", "GBP")).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void refusesToCompareAcrossCurrencies() {
        assertThatThrownBy(() -> Money.of("1", "GBP").compareTo(Money.of("1", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
