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
package org.apache.sling.testing.clients.executor.verifier;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Verifies that a JSON node exists with a given set of attribute names.
 */
public final class JsonNodeAttributesExistVerifier extends AbstractJsonNodeVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeAttributesExistVerifier.class);

    private final Set<String> attributeNames;

    public JsonNodeAttributesExistVerifier(final String jsonNodeToVerify, final Set<String> attributeNames) {
        super(jsonNodeToVerify);

        this.attributeNames = attributeNames;
    }

    public JsonNodeAttributesExistVerifier(final Set<String> attributeNames) {
        this(null, attributeNames);
    }

    @Override
    protected boolean verifyJsonNode(final JsonNode jsonNode) {
        boolean result = true;

        for (final String attributeName : attributeNames) {
            if (!jsonNode.has(attributeName)) {
                LOG.warn("attribute {} not found on JSON node: {}", attributeName, jsonNodeToVerify);

                result = false;
            }
        }

        return result;
    }
}
