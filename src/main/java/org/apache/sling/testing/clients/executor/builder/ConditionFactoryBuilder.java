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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.executor.listener.LoggingConditionEvaluationListener;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.pollinterval.IterativePollInterval;

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

    private final Map<Boolean, Function<Object, String>> valueToStringFunctions = new HashMap<>();

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
     * @param conditionSatisfied true if this function should apply when condition is satisified, false for function
     *     to apply when condition is not satisfied
     * @param valueToString to-string conversion function
     * @return this
     */
    public ConditionFactoryBuilder withValueToStringFunction(final boolean conditionSatisfied,
        final Function<Object, String> valueToString) {
        valueToStringFunctions.put(conditionSatisfied, valueToString);

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
            .conditionEvaluationListener(new LoggingConditionEvaluationListener(valueToStringFunctions))
            .pollDelay(delay)
            .pollInterval(IterativePollInterval.iterative(duration -> duration.multipliedBy(multiplier), initial))
            .ignoreExceptionsMatching(exception -> Arrays.stream(retryOn).anyMatch(retryOnException -> retryOnException
                .isAssignableFrom(exception.getClass())));
    }
}
