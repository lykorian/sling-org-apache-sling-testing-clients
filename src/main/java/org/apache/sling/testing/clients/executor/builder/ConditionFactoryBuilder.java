package org.apache.sling.testing.clients.executor.builder;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.executor.config.RetryConfig;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.EvaluatedCondition;
import org.awaitility.pollinterval.IterativePollInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for <code>ConditionFactory</code> instances with additional logging for each evaluation.
 */
public final class ConditionFactoryBuilder {

    /**
     * Get the default builder instance.
     *
     * @return builder
     */
    public static ConditionFactoryBuilder getInstance() {
        return new ConditionFactoryBuilder();
    }

    /**
     * Get a builder instance preconfigured using the provided <code>RetryConfig</code>.
     *
     * @param retryConfig retry configuration
     * @return builder
     */
    public static ConditionFactoryBuilder getInstance(final RetryConfig retryConfig) {
        return new ConditionFactoryBuilder(retryConfig);
    }

    private static final Logger LOG = LoggerFactory.getLogger(ConditionFactoryBuilder.class);

    private static final Duration DEFAULT_INITIAL = Duration.ofSeconds(1);

    private static final long DEFAULT_MULTIPLIER = 2;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    private String alias;

    private Duration delay = Duration.ZERO;

    private Duration initial = DEFAULT_INITIAL;

    private long multiplier = DEFAULT_MULTIPLIER;

    private Duration timeout = DEFAULT_TIMEOUT;

    private Function<Object, String> valueToString;

    private Class<?>[] retryOn = new Class[0];

    private ConditionFactoryBuilder() {

    }

    private ConditionFactoryBuilder(final RetryConfig retryConfig) {
        delay = retryConfig.getDelay();
        initial = retryConfig.getInitial();
        multiplier = retryConfig.getMultiplier();
        timeout = retryConfig.getTimeout();
        retryOn = retryConfig.getRetryOnExceptions();
    }

    /**
     * Set the alias to use for condition logging.
     *
     * @param alias alias
     * @return this
     */
    public ConditionFactoryBuilder withAlias(final String alias) {
        this.alias = alias;

        return this;
    }

    /**
     * Set the delay duration for retries.  Defaults to zero.
     *
     * @param delay delay duration
     * @return this
     */
    public ConditionFactoryBuilder withDelay(final Duration delay) {
        this.delay = delay;

        return this;
    }

    /**
     * Set the initial duration for retries.
     *
     * @param initial initial duration
     * @return this
     */
    public ConditionFactoryBuilder withInitial(final Duration initial) {
        this.initial = initial;

        return this;
    }

    /**
     * Set the multiplier for retries.
     *
     * @param multiplier multiplier value
     * @return this
     */
    public ConditionFactoryBuilder withMultiplier(final long multiplier) {
        this.multiplier = multiplier;

        return this;
    }

    /**
     * Set the timeout duration for retries.
     *
     * @param timeout timeout duration
     * @return this
     */
    public ConditionFactoryBuilder withTimeout(final Duration timeout) {
        this.timeout = timeout;

        return this;
    }

    /**
     * Set a function to use to convert the condition value to a <code>String</code> for logging.  By default, the
     * condition value will be logged using the value object's <code>toString</code> method.
     *
     * @param valueToString to-string conversion function
     * @return this
     */
    public ConditionFactoryBuilder withValueToStringFunction(final Function<Object, String> valueToString) {
        this.valueToString = valueToString;

        return this;
    }

    /**
     * Set the exception classes to retry on.  If these exceptions are thrown during evaluation, they will be ignored
     * and the condition will be retried.
     *
     * @param retryOn exception classes to retry on
     * @return this
     */
    public ConditionFactoryBuilder withRetryOn(final Class<?>[] retryOn) {
        this.retryOn = retryOn;

        return this;
    }

    /**
     * Build the condition factory.
     *
     * @return <code>ConditionFactory</code> instance
     */
    public ConditionFactory build() {
        final AtomicInteger count = new AtomicInteger(0);

        return Awaitility.await(alias)
            .atMost(timeout)
            .conditionEvaluationListener(getConditionEvaluationLogger(count.incrementAndGet()))
            .pollDelay(delay)
            .pollInterval(IterativePollInterval.iterative(duration -> duration.multipliedBy(multiplier), initial))
            .ignoreExceptionsMatching(getExceptionPredicate());
    }

    private Predicate<? super Throwable> getExceptionPredicate() {
        return ex -> {
            for (Class<?> ro : retryOn) {
                if (ex.getClass().isAssignableFrom(ro)) {
                    LOG.info("handling retryable exception: {}", ex.getClass().getName());
                    return true;
                }
            }
            LOG.error("encountered non-retryable exception", ex);
            return false;
        };
    }

    private ConditionEvaluationListener<?> getConditionEvaluationLogger(final int count) {
        return condition -> {
            if (condition.isSatisfied()) {
                LOG.info("{} satisfied after {} attempt(s) in {}ms, value: {}", getConditionLog(condition),
                    count, condition.getElapsedTimeInMS(), getStringValue(condition.getValue(),
                        valueToString));
            } else {
                LOG.info("{} not satisfied after {} attempt(s), poll interval: {}ms, elapsed time: {}ms, " +
                        "remaining time: {}ms, current value: {}", getConditionLog(condition), count,
                    condition.getPollInterval().toMillis(), condition.getElapsedTimeInMS(),
                    condition.getRemainingTimeInMS(), getStringValue(condition.getValue(), valueToString));
            }
        };
    }

    private String getConditionLog(final EvaluatedCondition<?> condition) {
        return condition.hasAlias() ? "condition [" + condition.getAlias() + "]" : "condition";
    }

    private String getStringValue(final Object conditionValue, final Function<Object, String> valueToString) {
        return Optional.ofNullable(valueToString)
            .map(function -> function.apply(conditionValue))
            .orElse(conditionValue.toString());
    }
}
