package dev.pricewatch.poll;

import dev.pricewatch.price.Money;

/**
 * Result of one poll. A failure is its own case, never a price of zero:
 * a parse failure read as 0 would fire a false alert.
 */
public sealed interface PollOutcome {

    String rawResponse();

    record Success(Money price, int httpStatus, String rawResponse) implements PollOutcome {
    }

    /** Fetched fine, but the page didn't contain a readable price. */
    record ParseFailure(String reason, int httpStatus, String rawResponse) implements PollOutcome {
    }

    /** Couldn't fetch: network error, timeout, non-2xx. {@code httpStatus} is null if no response. */
    record FetchFailure(String reason, Integer httpStatus, String rawResponse) implements PollOutcome {
    }
}
