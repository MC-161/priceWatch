package dev.pricewatch.poll;

import dev.pricewatch.price.PriceParser;
import dev.pricewatch.target.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Fetch, parse, store. Deliberately not @Transactional: no DB transaction
 * should stay open across a network call.
 */
@Service
public class PollService {

    private static final Logger log = LoggerFactory.getLogger(PollService.class);

    private final PageFetcher fetcher;
    private final PriceParser parser;
    private final PollRepository polls;

    public PollService(PageFetcher fetcher, PriceParser parser, PollRepository polls) {
        this.fetcher = fetcher;
        this.parser = parser;
        this.polls = polls;
    }

    public PollRecord poll(Target target) {
        PollOutcome outcome = fetchAndParse(target);
        log.info("polled target={} outcome={}", target.id(), outcome.getClass().getSimpleName());
        // TODO(M3): evaluate threshold here, in the same transaction as the insert
        return polls.insert(target.id(), outcome);
    }

    PollOutcome fetchAndParse(Target target) {
        PageFetcher.Page page;
        try {
            page = fetcher.get(target.url());
        } catch (RestClientException e) {
            return new PollOutcome.FetchFailure(e.getMessage(), null, null);
        }
        if (!page.ok()) {
            return new PollOutcome.FetchFailure("HTTP " + page.status(), page.status(), page.body());
        }
        return parser.parse(page.body(), target.selector(), target.threshold().currency())
                .<PollOutcome>map(price -> new PollOutcome.Success(price, page.status(), page.body()))
                .orElseGet(() -> new PollOutcome.ParseFailure(
                        "no price at selector " + target.selector(), page.status(), page.body()));
    }
}
