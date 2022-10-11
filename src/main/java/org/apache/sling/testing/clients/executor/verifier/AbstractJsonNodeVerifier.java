package org.apache.sling.testing.clients.executor.verifier;

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
    public final boolean verify(final JsonNode rootJsonNode) {
        final boolean result;

        if (jsonNodeToVerify == null) {
            LOG.debug("verifying root JSON node: {}", rootJsonNode.toString());

            result = verifyJsonNode(rootJsonNode);
        } else {
            LOG.debug("verifying path: {} for root JSON node: {}", jsonNodeToVerify, rootJsonNode.toString());

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
