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
import java.util.Set;

/**
 * Factory for JSON node verifiers.
 */
public final class JsonNodeVerifiers {

    /**
     * Get a JSON node verifier instance to verify that the JSON node has node with the given name.
     *
     * @param jsonNodeToVerify node name to verify exists
     * @return JSON node verifier
     */
    public static JsonNodeVerifier exists(final String jsonNodeToVerify) {
        return new JsonNodeExistsVerifier(jsonNodeToVerify, true);
    }

    /**
     * Get a JSON node verifier instance to verify that the JSON node does not have a node with the given name.
     *
     * @param jsonNodeToVerify node name to verify does not exist
     * @return JSON node verifier
     */
    public static JsonNodeVerifier doesNotExist(final String jsonNodeToVerify) {
        return new JsonNodeExistsVerifier(jsonNodeToVerify, false);
    }

    /**
     * Get a JSON node verifier instance to verify that the JSON node has the given attribute names.
     *
     * @param attributeNames attribute names to verify
     * @return JSON node verifier
     */
    public static JsonNodeVerifier hasAttributes(final Set<String> attributeNames) {
        return new JsonNodeAttributesExistVerifier(attributeNames);
    }

    /**
     * Get a JSON node verifier instance to verify that the JSON node has a node with the given name containing a set of
     * attribute names.
     *
     * @param jsonNodeToVerify node name containing the attributes
     * @param attributeNames attribute names to verify
     * @return JSON node verifier
     */
    public static JsonNodeVerifier hasAttributes(final String jsonNodeToVerify, final Set<String> attributeNames) {
        return new JsonNodeAttributesExistVerifier(jsonNodeToVerify, attributeNames);
    }

    /**
     * Get a JSON node verifier instance to verify that the JSON node has the given attribute name/value pairs.
     *
     * @param attributeMap attribute name/values to verify
     * @return JSON node verifier
     */
    public static JsonNodeVerifier hasAttributeValues(final Map<String, String> attributeMap) {
        return new JsonNodeWithAttributesVerifier(attributeMap);
    }

    /**
     * Get a JSON node verifier instance to verify that the JSON node has a node with the given name containing
     * attribute name/value pairs.
     *
     * @param jsonNodeToVerify node name containing the attributes
     * @param attributeMap attribute name/values to verify
     * @return JSON node verifier
     */
    public static JsonNodeVerifier hasAttributeValues(final String jsonNodeToVerify,
        final Map<String, String> attributeMap) {
        return new JsonNodeWithAttributesVerifier(jsonNodeToVerify, attributeMap);
    }

    /**
     * Static factory, do not allow instantiation.
     */
    private JsonNodeVerifiers() {

    }
}
