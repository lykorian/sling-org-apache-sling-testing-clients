package org.apache.sling.testing.clients.executor.verifier;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for JSON node verification.
 */
public interface JsonNodeVerifier {

    /**
     * Performs verification on the JSON node.
     *
     * @param rootNode root JSON node to verify
     * @return <code>true</code> if able to verify, <code>false</code> otherwise
     */
    boolean verify(@NotNull JsonNode rootNode);
}
