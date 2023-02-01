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
package org.apache.sling.testing.clients.executor.predicates;

import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.HttpServerRule;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlingHttpResponseBodyContainsPredicateTest {

    private static final String MOCK_RESPONSE = "Lorem ipsum";

    @ClassRule
    public static HttpServerRule httpServer = new HttpServerRule() {
        @Override
        protected void registerHandlers() {
            serverBootstrap.registerHandler("/test",
                (request, response, context) -> response.setEntity(new StringEntity(MOCK_RESPONSE)));
        }
    };

    private SlingClient client;

    @Before
    public void setup() throws ClientException {
        client = new SlingClient(httpServer.getURI(), "user", "pass");
    }

    @Test
    public void testMatch() throws ClientException {
        final SlingHttpResponse mockResponse = client.doGet("/test", HttpStatus.SC_OK);

        assertTrue(new SlingHttpResponseBodyContainsPredicate("Lorem").test(mockResponse));
    }

    @Test
    public void testNotMatch() throws ClientException {
        final SlingHttpResponse mockResponse = client.doGet("/test", HttpStatus.SC_OK);

        assertFalse(new SlingHttpResponseBodyContainsPredicate("123").test(mockResponse));
    }
}
