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
    public void mayRequestAndVerify(@NotNull final Callable<Boolean> request, @NotNull final Callable<Boolean> verifier)
        throws TestingValidationException {
        mayRequestAndVerify(null, request, verifier);
    }

    /**
     * Waits until a mutation is reflected. Will exit without sleeping or verifying if no mutation was performed.
     *
     * @param request the request to perform, return true if a mutation was made, false otherwise
     * @param verifier the verifier to call if a mutation was made
     */
    public void mayRequestAndVerify(final String alias, @NotNull final Callable<Boolean> request,
        @NotNull final Callable<Boolean> verifier) throws TestingValidationException {
        LOG.info("{}evaluating request...", getLogPrefix(alias));

        boolean requestPerformed;

        try {
            requestPerformed = request.call();
        } catch (Exception e) {
            throw new TestingValidationException("error calling request", e);
        }

        if (requestPerformed) {
            waitUntilReflected(alias, verifier);
        } else {
            LOG.debug("{}no request performed, not waiting", getLogPrefix(alias));
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
        requestAndVerify(null, request, verifier);
    }

    /**
     * Performs a mutation request, waits for a delay and verifies that the mutation is reflected.
     *
     * @param alias for logging
     * @param request the request to perform
     * @param verifier the verifier to ensure the mutation is reflected
     */
    public void requestAndVerify(final String alias, @NotNull final Callable<?> request,
        @NotNull final Callable<Boolean> verifier)
        throws TestingValidationException {
        LOG.info("{}sending request...", getLogPrefix(alias));

        try {
            request.call();
        } catch (Exception e) {
            throw new TestingValidationException("error calling request", e);
        }

        waitUntilReflected(alias, verifier);
    }

    private void waitUntilReflected(final String alias, final Callable<Boolean> verifier) throws TestingValidationException {
        LOG.info("{}waiting for request to be reflected...", getLogPrefix(alias));

        try {
            conditionFactory.until(verifier);
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout while waiting for request to be reflected", e);
        }
    }

    private String getLogPrefix(final String alias) {
        return alias == null ? "" : "[" + alias + "] ";
    }
}
