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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Base class for JSON node verifiers.
 */
public abstract class AbstractJsonNodeVerifier implements JsonNodeVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonNodeVerifier.class);

    protected final String jsonNodeToVerify;

    /**
     * Create a new verifier with the given JSON node path.
     *
     * @param jsonNodeToVerify path to node
     */
    protected AbstractJsonNodeVerifier(final String jsonNodeToVerify) {
        this.jsonNodeToVerify = jsonNodeToVerify;
    }

    /**
     * Verify the JSON node state.  If the JSON node is present, verification is delegated to the implementing class.
     *
     * @param rootJsonNode root JSON node to verify
     * @return true if JSON node state is verified
     */
    @Override
    public final boolean verify(@NotNull final JsonNode rootJsonNode) {
        final boolean result;

        if (jsonNodeToVerify == null) {
            LOG.info("verifying root JSON node: {}", rootJsonNode);

            result = verifyJsonNode(rootJsonNode);
        } else {
            LOG.info("verifying path: {} for root JSON node: {}", jsonNodeToVerify, rootJsonNode);

            if (rootJsonNode.path(jsonNodeToVerify).isMissingNode()) {
                LOG.warn("JSON node to verify attributes on does not exist: {}", jsonNodeToVerify);

                result = false;
            } else {
                final JsonNode foundNode = rootJsonNode.path(jsonNodeToVerify);

                result = verifyJsonNode(foundNode);
            }
        }

        return result;
    }

    /**
     * Evaluate the JSON node state.
     *
     * @param jsonNode JSON node to verify
     * @return true if JSON node matches the expected state
     */
    protected abstract boolean verifyJsonNode(final JsonNode jsonNode);
}
