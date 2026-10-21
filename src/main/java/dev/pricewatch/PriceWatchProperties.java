package dev.pricewatch;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricewatch")
public record PriceWatchProperties(String instanceId, Http http) {

    public record Http(String userAgent, Duration connectTimeout, Duration readTimeout) {
    }
}
