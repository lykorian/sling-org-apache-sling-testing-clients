package org.apache.sling.testing.clients.executor.predicates;

import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.SlingHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate for evaluating Sling HTTP response status code.
 */
public final class SlingHttpResponseStatusCodePredicate implements Predicate<SlingHttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SlingHttpResponseStatusCodePredicate.class);

    private final int[] expectedStatus;

    public SlingHttpResponseStatusCodePredicate(final int... expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    @Override
    public boolean test(final SlingHttpResponse response) {
        boolean isExpectedStatus = true;

        if (expectedStatus.length > 0) {
            final int statusCode = response.getStatusLine().getStatusCode();

            isExpectedStatus = Arrays.stream(expectedStatus).anyMatch(ex -> ex == statusCode);

            if (isExpectedStatus) {
                LOG.debug("response status code: {}", statusCode);
            } else {
                LOG.warn("unexpected response status code: {}", statusCode);
            }
        } else {
            LOG.debug("expected status not provided, returning true");
        }

        return isExpectedStatus;
    }
}
