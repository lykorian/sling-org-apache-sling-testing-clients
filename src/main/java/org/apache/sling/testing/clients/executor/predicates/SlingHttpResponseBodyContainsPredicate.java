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

import java.util.function.Predicate;

import org.apache.sling.testing.clients.SlingHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate for evaluating Sling HTTP response body contents.
 */
public final class SlingHttpResponseBodyContainsPredicate implements Predicate<SlingHttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SlingHttpResponseBodyContainsPredicate.class);

    private final String expected;

    public SlingHttpResponseBodyContainsPredicate(final String expected) {
        this.expected = expected;
    }

    @Override
    public boolean test(final SlingHttpResponse response) {
        boolean matches = true;

        if (expected != null) {
            final String content = response.getContent();

            matches = content.contains(expected);

            if (matches) {
                LOG.debug("expected content: {}, response body: {}", expected, content);
            } else {
                LOG.warn("response does not contain expected content: {}, response body: {}", expected, content);
            }
        }

        return matches;
    }
}
