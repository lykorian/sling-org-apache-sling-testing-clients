package org.apache.sling.testing.clients.executor.config;

import java.io.IOException;
import java.time.Duration;

import org.apache.sling.testing.clients.ClientException;

/**
 * Configuration for retrying requests.
 */
public final class RetryConfig {

    /** Default initial duration for retries. */
    private static final Duration DEFAULT_INITIAL = Duration.ofSeconds(1);

    /** Default multiplier for retries. */
    private static final long DEFAULT_MULTIPLIER = 2;

    /** Default timeout for retries. */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /** Default exceptions to retry on. */
    private static final Class<?>[] DEFAULT_RETRY_ON_EXCEPTIONS = {ClientException.class, IOException.class};

    public static final RetryConfig DEFAULT = new RetryConfig();

    private final Duration delay;

    private final Duration initial;

    private final long multiplier;

    private final Duration timeout;

    private final Class<?>[] retryOnExceptions;

    private RetryConfig() {
        this.delay = Duration.ZERO;
        this.initial = DEFAULT_INITIAL;
        this.multiplier = DEFAULT_MULTIPLIER;
        this.timeout = DEFAULT_TIMEOUT;
        this.retryOnExceptions = DEFAULT_RETRY_ON_EXCEPTIONS;
    }

    private RetryConfig(final Duration delay, final Duration initial, final long multiplier, final Duration timeout,
        final Class<?>[] retryOnExceptions) {
        this.delay = delay;
        this.initial = initial;
        this.multiplier = multiplier;
        this.timeout = timeout;
        this.retryOnExceptions = retryOnExceptions;
    }

    public Duration getDelay() {
        return delay;
    }

    public Duration getInitial() {
        return initial;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Class<?>[] getRetryOnExceptions() {
        return retryOnExceptions;
    }

    public RetryConfig withDelay(final Duration delay) {
        return new RetryConfig(delay, initial, multiplier, timeout, retryOnExceptions);
    }

    public RetryConfig withInitial(final Duration initial) {
        return new RetryConfig(delay, initial, multiplier, timeout, retryOnExceptions);
    }

    public RetryConfig withMultiplier(final long multiplier) {
        return new RetryConfig(delay, initial, multiplier, timeout, retryOnExceptions);
    }

    public RetryConfig withTimeout(final Duration timeout) {
        return new RetryConfig(delay, initial, multiplier, timeout, retryOnExceptions);
    }

    public RetryConfig withRetryOnExceptions(final Class<?>... retryOnExceptions) {
        return new RetryConfig(delay, initial, multiplier, timeout, retryOnExceptions);
    }
}
