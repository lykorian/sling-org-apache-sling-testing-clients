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