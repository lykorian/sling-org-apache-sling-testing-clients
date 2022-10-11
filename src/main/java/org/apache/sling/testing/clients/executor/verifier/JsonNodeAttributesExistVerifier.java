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