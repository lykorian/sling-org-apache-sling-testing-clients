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
