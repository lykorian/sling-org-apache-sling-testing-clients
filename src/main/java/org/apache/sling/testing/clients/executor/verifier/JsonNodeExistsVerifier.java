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
