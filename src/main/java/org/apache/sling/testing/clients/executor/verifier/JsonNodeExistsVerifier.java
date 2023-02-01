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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Verifies that a JSON node does or does not exist.
 */
public final class JsonNodeExistsVerifier implements JsonNodeVerifier {

    private final String nodeToVerify;

    private final Boolean shouldExist;

    public JsonNodeExistsVerifier(final String nodeToVerify, final boolean shouldExist) {
        this.nodeToVerify = nodeToVerify;
        this.shouldExist = shouldExist;
    }

    @Override
    public boolean verify(@NotNull final JsonNode rootJsonNode) {
        return rootJsonNode.path(nodeToVerify).isMissingNode() != shouldExist;
    }
}
