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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Verifies that a JSON node exists with attribute values.
 */
public final class JsonNodeWithAttributesVerifier extends AbstractJsonNodeVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeWithAttributesVerifier.class);

    private final Map<String, String> attributeMap;

    public JsonNodeWithAttributesVerifier(final String jsonNodeToVerify, final Map<String, String> attributeMap) {
        super(jsonNodeToVerify);

        this.attributeMap = attributeMap;
    }

    public JsonNodeWithAttributesVerifier(final Map<String, String> attributeMap) {
        this(null, attributeMap);
    }

    @Override
    protected boolean verifyJsonNode(final JsonNode jsonNode) {
        boolean result = true;

        for (final String attributeName : attributeMap.keySet()) {
            final JsonNode attributeNode = jsonNode.path(attributeName);

            if (attributeNode.isMissingNode()) {
                LOG.warn("JSON node to verify: {} does not contain attribute name: {}", jsonNodeToVerify,
                    attributeName);

                result = false;
                break;
            } else if (!attributeNode.textValue().equals(attributeMap.get(attributeName))) {
                LOG.warn("JSON node to verify: {} does not contain attribute name: {} with value: {}", jsonNodeToVerify,
                    attributeName, attributeMap.get(attributeName));

                result = false;
                break;
            }
        }

        return result;
    }
}
