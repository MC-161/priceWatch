/**
 * M4: resilience against upstreams. Per-source backoff on 429, a circuit breaker for persistent
 * failure, and isolation so one bad source never delays others. Cap v1 at two sources; respect robots.txt.
 */
package dev.pricewatch.source;
