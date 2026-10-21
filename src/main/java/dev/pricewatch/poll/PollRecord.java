package dev.pricewatch.poll;

import java.math.BigDecimal;
import java.time.Instant;

/** A stored poll, as exposed by the history API. Raw response is omitted here. */
public record PollRecord(long id, long targetId, Instant polledAt, String status, BigDecimal price,
                         Integer httpStatus, String error) {
}
