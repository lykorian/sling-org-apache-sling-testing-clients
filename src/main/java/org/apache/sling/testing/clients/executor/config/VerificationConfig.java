package org.apache.sling.testing.clients.executor.config;

import java.time.Duration;

/**
 * Configuration for verifying requests.
 */
public final class VerificationConfig {

    /** Default initial duration for verifying requests. */
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(2);

    /** Default initial duration for retries. */
    private static final Duration DEFAULT_INITIAL = Duration.ofSeconds(1);

    /** Default multiplier for retries. */
    private static final long DEFAULT_MULTIPLIER = 2;

    /** Default timeout for retries. */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public static VerificationConfig DEFAULT = new VerificationConfig();

    private final Duration delay;

    private final Duration initial;

    private final long multiplier;

    private final Duration timeout;

    private VerificationConfig() {
        delay = DEFAULT_DELAY;
        initial = DEFAULT_INITIAL;
        multiplier = DEFAULT_MULTIPLIER;
        timeout = DEFAULT_TIMEOUT;
    }

    private VerificationConfig(final Duration delay, final Duration initial, final long multiplier,
        final Duration timeout) {
        this.delay = delay;
        this.initial = initial;
        this.multiplier = multiplier;
        this.timeout = timeout;
    }

    /**
     * Get the verification delay.
     *
     * @return delay duration
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Get the initial verification delay.
     *
     * @return initial delay duration
     */
    public Duration getInitial() {
        return initial;
    }

    /**
     * Get the verification timeout.
     *
     * @return timeout duration
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Get the multiplier value for exponential delay backoff.
     *
     * @return multiplier
     */
    public long getMultiplier() {
        return multiplier;
    }

    /**
     * Builder method to configure the delay.
     *
     * @param delay delay duration
     * @return this
     */
    public VerificationConfig withDelay(final Duration delay) {
        return new VerificationConfig(delay, initial, multiplier, timeout);
    }

    /**
     * Builder method to configure the initial delay.
     *
     * @param initial initial delay duration
     * @return this
     */
    public VerificationConfig withInitial(final Duration initial) {
        return new VerificationConfig(delay, initial, multiplier, timeout);
    }

    /**
     * Builder method to configure the multiplier.
     *
     * @param multiplier multiplier value
     * @return this
     */
    public VerificationConfig withMultiplier(final long multiplier) {
        return new VerificationConfig(delay, initial, multiplier, timeout);
    }

    /**
     * Builder method to configure the timeout.
     *
     * @param timeout timeout duration
     * @return this
     */
    public VerificationConfig withTimeout(final Duration timeout) {
        return new VerificationConfig(delay, initial, multiplier, timeout);
    }
}
