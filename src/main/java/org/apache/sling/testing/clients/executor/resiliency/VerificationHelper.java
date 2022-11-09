package org.apache.sling.testing.clients.executor.resiliency;

import java.util.concurrent.Callable;

import org.apache.sling.testing.clients.exceptions.TestingValidationException;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request verification helper.
 */
public final class VerificationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationHelper.class);

    private final ConditionFactory conditionFactory;

    public VerificationHelper(@NotNull final ConditionFactory conditionFactory) {
        this.conditionFactory = conditionFactory;
    }

    /**
     * Waits until a mutation is reflected. Will exit without sleeping or verifying if no mutation was performed.
     *
     * @param request the request to perform, return true if a mutation was made, false otherwise
     * @param verifier the verifier to call if a mutation was made
     */
    public void mayRequestAndVerify(@NotNull final Callable<Boolean> request,
        @NotNull final Callable<Boolean> verifier) throws TestingValidationException {
        LOG.info("sending request...");

        boolean requestPerformed;

        try {
            requestPerformed = request.call();
        } catch (Exception e) {
            throw new TestingValidationException("error calling request", e);
        }

        if (requestPerformed) {
            waitUntilReflected(verifier);
        } else {
            LOG.debug("no request performed, not waiting");
        }
    }

    /**
     * Performs a mutation request, waits for a delay and verifies that the mutation is reflected.
     *
     * @param request the request to perform
     * @param verifier the verifier to ensure the mutation is reflected
     */
    public void requestAndVerify(@NotNull final Callable<?> request, @NotNull final Callable<Boolean> verifier)
        throws TestingValidationException {
        LOG.info("sending request...");

        try {
            request.call();
        } catch (Exception e) {
            throw new TestingValidationException("error calling request", e);
        }

        waitUntilReflected(verifier);
    }

    private void waitUntilReflected(final Callable<Boolean> verifier) throws TestingValidationException {
        LOG.info("waiting for request to be reflected...");

        try {
            conditionFactory.until(verifier);
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout while waiting for request to be reflected", e);
        }
    }
}
