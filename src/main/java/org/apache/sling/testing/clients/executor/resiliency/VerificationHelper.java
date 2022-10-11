package org.apache.sling.testing.clients.executor.resiliency;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.sling.testing.clients.executor.builder.ConditionFactoryBuilder;
import org.apache.sling.testing.clients.executor.config.VerificationConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationHelper.class);

    private final Duration delay;
    private final Duration initial;
    private final long multiplier;
    private final Duration timeout;

    public VerificationHelper(@NotNull final VerificationConfig config) {
        delay = config.getDelay();
        initial = config.getInitial();
        multiplier = config.getMultiplier();
        timeout = config.getTimeout();
    }

    /**
     * Waits until a mutation is reflected. Will exit without sleeping or verifying if no mutation was performed.
     *
     * @param request the request to perform, return true if a mutation was made, false otherwise
     * @param verifier the verifier to call if a mutation was made
     */
    public final void mayRequestAndVerify(@NotNull final Callable<Boolean> request,
        @NotNull final Callable<Boolean> verifier) {
        LOG.info("Sending request...");

        boolean requestPerformed;

        try {
            requestPerformed = request.call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call request", e);
        }

        if (requestPerformed) {
            sleep();
            waitUntilReflected(verifier);
        } else {
            LOG.debug("No request performed, not waiting");
        }
    }

    /**
     * Performs a mutation request, waits for a delay and verifies that the mutation is reflected.
     *
     * @param request the request to perform
     * @param verifier the verifier to ensure the mutation is reflected
     */
    public final void requestAndVerify(@NotNull final Callable<?> request, @NotNull final Callable<Boolean> verifier) {
        LOG.info("Sending request...");

        try {
            request.call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call request", e);
        }

        sleep();
        waitUntilReflected(verifier);
    }

    private void sleep() {
        LOG.info("Waiting for initial duration of {}ms before verifying...", delay.toMillis());

        try {
            TimeUnit.MILLISECONDS.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            LOG.error("Initial verification delay interrupted", e);

            Thread.currentThread().interrupt();
        }
    }

    private void waitUntilReflected(final Callable<Boolean> verifier) {
        LOG.info("Waiting for request to be reflected...");

        ConditionFactoryBuilder.getInstance()
            .withInitial(initial)
            .withMultiplier(multiplier)
            .withTimeout(timeout)
            .build()
            .until(verifier);
    }
}
