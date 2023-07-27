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
     * Performs a mutation request, waits for a delay and verifies that the mutation is reflected.
     *
     * @param request the request to perform
     * @param verifier the verifier to ensure the mutation is reflected
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if error occurs during request
     */
    public <T> T requestAndVerify(@NotNull final Callable<T> request, @NotNull final Callable<Boolean> verifier)
        throws TestingValidationException {
        return requestAndVerify(request, verifier, null);
    }

    /**
     * Performs a mutation request, waits for a delay and verifies that the mutation is reflected.
     *
     * @param request the request to perform
     * @param verifier the verifier to ensure the mutation is reflected
     * @param alias for logging
     * @param aliasArgs arguments referenced in alias (optional)
     * @param <T> response type
     * @return response
     * @throws TestingValidationException if error occurs during request
     */
    public <T> T requestAndVerify(@NotNull final Callable<T> request, @NotNull final Callable<Boolean> verifier,
        final String alias, final Object... aliasArgs)
        throws TestingValidationException {
        LOG.info("{}sending request...", getLogPrefix(alias, aliasArgs));

        final T response;

        try {
            response = request.call();
        } catch (Exception e) {
            throw new TestingValidationException("error calling request", e);
        }

        verify(verifier, alias);

        return response;
    }

    private void verify(final Callable<Boolean> verifier, final String alias, final Object... aliasArgs)
        throws TestingValidationException {
        LOG.info("{}verifying request...", getLogPrefix(alias, aliasArgs));

        try {
            conditionFactory.until(verifier);
        } catch (ConditionTimeoutException e) {
            throw new TestingValidationException("timeout while waiting for request to be reflected", e);
        }
    }

    private String getLogPrefix(final String alias, final Object... aliasArgs) {
        return alias == null ? "" : "[" + String.format(alias, aliasArgs) + "] ";
    }
}
