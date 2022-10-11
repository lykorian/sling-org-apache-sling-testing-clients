package org.apache.sling.testing.clients.executor.predicates;

import java.util.function.Predicate;

import org.apache.sling.testing.clients.SlingHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate for evaluating Sling HTTP response body contents.
 */
public final class SlingHttpResponseBodyContainsPredicate implements Predicate<SlingHttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SlingHttpResponseBodyContainsPredicate.class);

    private final String expected;

    public SlingHttpResponseBodyContainsPredicate(final String expected) {
        this.expected = expected;
    }

    @Override
    public boolean test(final SlingHttpResponse response) {
        boolean matches = true;

        if (expected != null) {
            final String content = response.getContent();

            matches = content.contains(expected);

            if (matches) {
                LOG.debug("expected content: {}, response body: {}", expected, content);
            } else {
                LOG.warn("response does not contain expected content: {}, response body: {}", expected, content);
            }
        }

        return matches;
    }
}
