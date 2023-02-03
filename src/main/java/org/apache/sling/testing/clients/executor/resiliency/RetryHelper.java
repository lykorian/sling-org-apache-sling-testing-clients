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
package org.apache.sling.testing.clients.executor.resiliency;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.exceptions.TestingValidationException;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;

import static org.hamcrest.Matchers.anything;

/**
 * Request retry helper.
 */
public final class RetryHelper {

    private final ConditionFactory defaultConditionFactory;

    public RetryHelper(@NotNull final ConditionFactory conditionFactory) {
        defaultConditionFactory = conditionFactory;
    }

    /**
     * Retry the request until it exists (i.e. returns a non-null response).
     *
     * @param request request
     * @return response
     */
    public <T> T retryUntilExists(@NotNull final Callable<T> request) throws TestingValidationException {
        return retryUntilCondition(request, Objects::nonNull);
    }

    /**
     * Retry the request until it exists (i.e. returns a non-null response).
     *
     * @param request request
     * @param conditionFactory condition factory
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public <T> T retryUntilExists(@NotNull final Callable<T> request,
        @NotNull final ConditionFactory conditionFactory) throws TestingValidationException {
        return retryUntilCondition(request, Objects::nonNull, conditionFactory);
    }

    /**
     * Retry the request until the response meets the provided condition.
     *
     * @param request request
     * @param condition predicate for evaluating response
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public <T> T retryUntilCondition(@NotNull final Callable<T> request, @NotNull final Predicate<T> condition)
        throws TestingValidationException {
        return retryUntilCondition(request, condition, defaultConditionFactory);
    }

    /**
     * Retry the request until the response meets the provided condition.
     *
     * @param request request
     * @param condition condition for evaluating response
     * @param conditionFactory condition factory
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public <T> T retryUntilCondition(@NotNull final Callable<T> request, @NotNull final Predicate<T> condition,
        @NotNull final ConditionFactory conditionFactory) throws TestingValidationException {
        try {
            return conditionFactory.until(request, condition);
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout waiting for condition", e);
        }
    }

    /**
     * Retry the request until an exception is not thrown.
     *
     * @param request request
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public <T> T retryUntilExceptionNotThrown(@NotNull final Callable<T> request) throws TestingValidationException {
        return retryUntilExceptionNotThrown(request, defaultConditionFactory);
    }

    /**
     * Retry the request until an exception is not thrown.
     *
     * @param request request
     * @param conditionFactory condition factory
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public <T> T retryUntilExceptionNotThrown(@NotNull final Callable<T> request,
        @NotNull final ConditionFactory conditionFactory) throws TestingValidationException {
        try {
            return conditionFactory.until(request, anything());
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout waiting for condition", e);
        }
    }

    /**
     * Retry the request until it returns <code>true</code>.
     *
     * @param request request
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public void retryUntilTrue(@NotNull final Callable<Boolean> request) throws TestingValidationException {
        retryUntilTrue(request, defaultConditionFactory);
    }

    /**
     * Retry the request until it returns <code>true</code>.
     *
     * @param request request
     * @param conditionFactory condition factory
     * @throws TestingValidationException if timeout is reached and condition is not met
     */
    public void retryUntilTrue(@NotNull final Callable<Boolean> request,
        @NotNull final ConditionFactory conditionFactory) throws TestingValidationException {
        try {
            conditionFactory.until(request);
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout waiting for condition", e);
        }
    }
}
