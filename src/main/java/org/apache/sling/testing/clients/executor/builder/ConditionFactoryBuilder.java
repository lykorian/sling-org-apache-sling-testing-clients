/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.testing.clients.executor.builder;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.executor.listener.LoggingConditionEvaluationListener;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.pollinterval.IterativePollInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for <code>ConditionFactory</code> instances with additional logging for each evaluation.
 */
public final class ConditionFactoryBuilder {

    /**
     * Get a builder instance with the default configuration.
     *
     * @return builder
     */
    public static ConditionFactoryBuilder getInstance() {
        return new ConditionFactoryBuilder();
    }

    private static final Logger LOG = LoggerFactory.getLogger(ConditionFactoryBuilder.class);

    /** Default initial duration for retries. */
    private static final Duration DEFAULT_INITIAL = Duration.ofSeconds(1);

    /** Default multiplier for retries. */
    private static final long DEFAULT_MULTIPLIER = 2;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    /** Default exceptions to retry on. */
    private static final Class<?>[] DEFAULT_RETRY_ON_EXCEPTIONS = {ClientException.class, IOException.class};

    private String alias;

    private Duration delay;

    private Duration initial;

    private long multiplier;

    private Duration timeout;

    private Class<?>[] retryOn;

    private Function<Object, String> valueToString;

    private ConditionFactoryBuilder() {
        delay = Duration.ZERO;
        initial = DEFAULT_INITIAL;
        multiplier = DEFAULT_MULTIPLIER;
        timeout = DEFAULT_TIMEOUT;
        retryOn = DEFAULT_RETRY_ON_EXCEPTIONS;
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
    public ConditionFactoryBuilder withRetryOn(final Class<?>... retryOn) {
        this.retryOn = retryOn;

        return this;
    }

    /**
     * Build the condition factory.
     *
     * @return <code>ConditionFactory</code> instance
     */
    public ConditionFactory build() {
        return Awaitility.await(alias)
            .atMost(timeout)
            .conditionEvaluationListener(new LoggingConditionEvaluationListener(valueToString))
            .pollDelay(delay)
            .pollInterval(IterativePollInterval.iterative(duration -> duration.multipliedBy(multiplier), initial))
            .ignoreExceptionsMatching(getExceptionPredicate());
    }

    private Predicate<? super Throwable> getExceptionPredicate() {
        return exception -> {
            final boolean retryable = Arrays.stream(retryOn).anyMatch(retryOnException -> retryOnException
                .isAssignableFrom(exception.getClass()));

            if (retryable) {
                LOG.info("handling retryable exception: {}", exception.getClass().getName());
            } else {
                LOG.error("encountered non-retryable exception", exception);
            }

            return retryable;
        };
    }
}
