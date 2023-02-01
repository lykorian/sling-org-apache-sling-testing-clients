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
package org.apache.sling.testing.clients.executor.predicates;

import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.SlingHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate for evaluating Sling HTTP response status code.
 */
public final class SlingHttpResponseStatusCodePredicate implements Predicate<SlingHttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SlingHttpResponseStatusCodePredicate.class);

    private final int[] expectedStatus;

    public SlingHttpResponseStatusCodePredicate(final int... expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    @Override
    public boolean test(final SlingHttpResponse response) {
        boolean isExpectedStatus = true;

        if (expectedStatus.length > 0) {
            final int statusCode = response.getStatusLine().getStatusCode();

            isExpectedStatus = Arrays.stream(expectedStatus).anyMatch(ex -> ex == statusCode);

            if (isExpectedStatus) {
                LOG.debug("response status code: {}", statusCode);
            } else {
                LOG.warn("unexpected response status code: {}", statusCode);
            }
        } else {
            LOG.debug("expected status not provided, returning true");
        }

        return isExpectedStatus;
    }
}
