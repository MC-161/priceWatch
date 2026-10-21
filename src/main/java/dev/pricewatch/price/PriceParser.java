package dev.pricewatch.price;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extracts a price from HTML with a CSS selector.
 * Returns empty when the selector matches nothing or the text isn't a price;
 * callers must treat empty as an error, not as zero.
 */
@Component
public class PriceParser {

    // e.g. "£1,299.99", "$ 49.00", "49.99 GBP"
    private static final Pattern NUMBER = Pattern.compile("\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|\\d+(?:\\.\\d+)?");

    public Optional<Money> parse(String html, String selector, Currency currency) {
        Element el = Jsoup.parse(html).selectFirst(selector);
        if (el == null) {
            return Optional.empty();
        }
        String text = el.hasAttr("content") ? el.attr("content") : el.text();
        Matcher m = NUMBER.matcher(text);
        if (!m.find()) {
            return Optional.empty();
        }
        // TODO: locale-aware parsing ("1.299,99") once a source needs it
        BigDecimal amount = new BigDecimal(m.group().replace(",", ""));
        return Optional.of(new Money(amount, currency));
    }
}
