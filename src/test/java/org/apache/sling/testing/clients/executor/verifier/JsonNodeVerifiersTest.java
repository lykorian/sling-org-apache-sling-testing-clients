package org.apache.sling.testing.clients.executor.verifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.sling.testing.clients.exceptions.TestingIOException;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.Assert.*;

public class JsonNodeVerifiersTest {

    @Test
    public void testExists() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"test\": \"test\"}");

        assertTrue(JsonNodeVerifiers.exists("test").verify(jsonNode));
    }

    @Test
    public void testDoesNotExist() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"test\": \"test\"}");

        assertFalse(JsonNodeVerifiers.doesNotExist("test").verify(jsonNode));
    }

    @Test
    public void testHasAttributes() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"one\": \"1\", \"two\": \"2\"}");

        final Set<String> attributes = new HashSet<>(Arrays.asList("one", "two"));

        assertTrue(JsonNodeVerifiers.hasAttributes(attributes).verify(jsonNode));
    }

    @Test
    public void testHasAttributesForNode() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"test\":{\"one\": \"1\", \"two\": \"2\"}}");

        final Set<String> attributes = new HashSet<>(Arrays.asList("one", "two"));

        assertTrue(JsonNodeVerifiers.hasAttributes("test", attributes).verify(jsonNode));
    }

    @Test
    public void testHasAttributeValues() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"one\": \"1\", \"two\": \"2\"}");

        final Map<String, String> attributesMap = new HashMap<>();

        attributesMap.put("one", "1");
        attributesMap.put("two", "2");

        assertTrue(JsonNodeVerifiers.hasAttributeValues(attributesMap).verify(jsonNode));
    }

    @Test
    public void testHasAttributeValuesForNode() throws TestingIOException {
        final JsonNode jsonNode = JsonUtils.getJsonNodeFromString("{\"test\":{\"one\": \"1\", \"two\": \"2\"}}");

        final Map<String, String> attributesMap = new HashMap<>();

        attributesMap.put("one", "1");
        attributesMap.put("two", "2");

        assertTrue(JsonNodeVerifiers.hasAttributeValues("test", attributesMap).verify(jsonNode));
    }
}