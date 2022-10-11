package org.apache.sling.testing.clients.executor.verifier;

import java.util.Map;
import java.util.Set;

/**
 * Factory for JSON node verifiers.
 */
public final class JsonNodeVerifiers {

    public static JsonNodeVerifier exists(final String jsonNodeToVerify) {
        return new JsonNodeExistsVerifier(jsonNodeToVerify, true);
    }

    public static JsonNodeVerifier doesNotExist(final String jsonNodeToVerify) {
        return new JsonNodeExistsVerifier(jsonNodeToVerify, false);
    }

    public static JsonNodeVerifier hasAttributes(final Set<String> attributeNames) {
        return new JsonNodeAttributesExistVerifier(attributeNames);
    }

    public static JsonNodeVerifier hasAttributes(final String jsonNodeToVerify, final Set<String> attributeNames) {
        return new JsonNodeAttributesExistVerifier(jsonNodeToVerify, attributeNames);
    }

    public static JsonNodeVerifier hasAttributeValues(final Map<String, String> attributeMap) {
        return new JsonNodeWithAttributesVerifier(attributeMap);
    }

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
