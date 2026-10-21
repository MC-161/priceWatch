package dev.pricewatch.poll;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pricewatch.PostgresTestConfig;
import dev.pricewatch.StubSite;
import dev.pricewatch.target.Target;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

/** M0 verify: one target polled by hand, stored, history readable; broken selector is an error, not zero. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfig.class)
class ManualPollIT {

    static final StubSite site = new StubSite()
            .respond("/item", 200, "<html><span class='price'>£42.50</span></html>")
            .respond("/blocked", 429, "slow down");

    @Autowired
    TestRestTemplate rest;

    @AfterAll
    static void stop() {
        site.close();
    }

    @Test
    void pollStoresPriceAndHistoryIsReadable() {
        long id = createTarget(site.url("/item"), ".price");

        PollRecord polled = rest.postForObject("/targets/{id}/poll", null, PollRecord.class, id);
        assertThat(polled.status()).isEqualTo("OK");
        assertThat(polled.price()).isEqualByComparingTo("42.50");

        assertThat(history(id)).singleElement().extracting(PollRecord::price)
                .satisfies(p -> assertThat(p).isEqualByComparingTo("42.50"));
    }

    @Test
    void brokenSelectorIsAnErrorNotZero() {
        long id = createTarget(site.url("/item"), ".no-such-element");

        PollRecord polled = rest.postForObject("/targets/{id}/poll", null, PollRecord.class, id);
        assertThat(polled.status()).isEqualTo("PARSE_ERROR");
        assertThat(polled.price()).isNull();
    }

    @Test
    void rateLimitedResponseIsAFetchError() {
        long id = createTarget(site.url("/blocked"), ".price");

        PollRecord polled = rest.postForObject("/targets/{id}/poll", null, PollRecord.class, id);
        assertThat(polled.status()).isEqualTo("FETCH_ERROR");
        assertThat(polled.httpStatus()).isEqualTo(429);
        assertThat(polled.price()).isNull();
    }

    private long createTarget(String url, String selector) {
        var body = Map.of("url", url, "selector", selector, "threshold", new BigDecimal("40.00"),
                "currency", "GBP", "intervalSeconds", 900);
        return rest.postForObject("/targets", body, Target.class).id();
    }

    private List<PollRecord> history(long id) {
        return rest.exchange("/targets/{id}/history", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<PollRecord>>() {}, id).getBody();
    }
}
