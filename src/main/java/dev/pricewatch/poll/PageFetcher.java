package dev.pricewatch.poll;

import dev.pricewatch.PriceWatchProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Plain HTTP GET. No retries or backoff yet; that's M4. */
@Component
public class PageFetcher {

    public record Page(int status, String body) {
        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    private final RestClient http;

    public PageFetcher(RestClient.Builder builder, PriceWatchProperties props) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.http().connectTimeout());
        factory.setReadTimeout(props.http().readTimeout());
        this.http = builder
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, props.http().userAgent())
                .build();
    }

    /** Returns the response whatever its status; throws only on I/O failure. */
    public Page get(String url) {
        return http.get().uri(url).exchange((req, res) ->
                new Page(res.getStatusCode().value(), new String(res.getBody().readAllBytes())));
    }
}
