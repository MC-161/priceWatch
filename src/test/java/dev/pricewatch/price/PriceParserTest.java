package dev.pricewatch.price;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;
import org.junit.jupiter.api.Test;

class PriceParserTest {

    private static final Currency GBP = Currency.getInstance("GBP");
    private final PriceParser parser = new PriceParser();

    @Test
    void parsesFormattedPrice() {
        String html = "<span class='price'>£1,299.99</span>";
        assertThat(parser.parse(html, ".price", GBP)).contains(Money.of("1299.99", "GBP"));
    }

    @Test
    void prefersContentAttribute() {
        String html = "<meta itemprop='price' content='49.00'><span>was £60</span>";
        assertThat(parser.parse(html, "[itemprop=price]", GBP)).contains(Money.of("49", "GBP"));
    }

    @Test
    void brokenSelectorIsEmptyNotZero() {
        String html = "<span class='price'>£49.99</span>";
        assertThat(parser.parse(html, ".does-not-exist", GBP)).isEmpty();
    }

    @Test
    void nonNumericTextIsEmptyNotZero() {
        String html = "<span class='price'>Out of stock</span>";
        assertThat(parser.parse(html, ".price", GBP)).isEmpty();
    }
}
