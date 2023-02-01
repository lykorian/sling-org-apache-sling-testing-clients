package org.apache.sling.testing.clients.executor;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.HttpServerRule;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.exceptions.TestingValidationException;
import org.apache.sling.testing.clients.executor.builder.ConditionFactoryBuilder;
import org.apache.sling.testing.clients.resiliency.ResilientSlingClient;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.util.HttpRequestUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequestExecutorTest {

    @ClassRule
    public static HttpServerRule httpServer = new HttpServerRule() {
        @Override
        protected void registerHandlers() {
            serverBootstrap.registerHandler("/test", (request, response, context) -> {
                for (final Header header : request.getAllHeaders()) {
                    response.setHeader(header);
                }

                response.setEntity(new StringEntity(getResponseBody(request)));
            });

            serverBootstrap.registerHandler("/form", (request, response, context) -> {
                if (HttpPost.METHOD_NAME.equals(request.getRequestLine().getMethod())) {
                    response.setEntity(new StringEntity(getResponseBody(request)));
                    response.setStatusCode(HttpStatus.SC_CREATED);
                } else {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                }
            });

            serverBootstrap.registerHandler("/redirect", (request, response, context) -> {
                response.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
                response.setHeader(HttpHeaders.LOCATION, "/test");
            });

            serverBootstrap.registerHandler("/noretry", (request, response, context) -> {
                response.setEntity(new StringEntity(getResponseBody(request)));
            });
        }
    };

    private ResilientSlingClient client;

    @Before
    public void setup() throws ClientException {
        client = new ResilientSlingClient(httpServer.getURI(), "user", "pass");
    }

    @Test
    public void testRequest() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/test").execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test", response.getContent());
    }

    @Test
    public void testRequestPath() throws TestingValidationException {
        final SlingHttpResponse response = new RequestExecutor(client, HttpGet.METHOD_NAME)
            .path("/test")
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test", response.getContent());
    }

    @Test
    public void testRequestUri() throws TestingValidationException {
        final URI uri = client.getUrl("/test");

        final SlingHttpResponse response = new RequestExecutor(client, HttpGet.METHOD_NAME)
            .uri(uri)
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test", response.getContent());
    }

    @Test
    public void testRequestExpectedStatus() throws TestingValidationException {
        client.get("/test").withExpectedStatus(HttpStatus.SC_OK).execute();
    }

    @Test
    public void testRequestExpectedContent() throws TestingValidationException {
        client.get("/test").withExpectedContent("test").execute();
    }

    @Test
    public void testRequestExpectedCondition() throws TestingValidationException {
        client.get("/test").withExpectedCondition(response -> "test".equals(response.getContent())).execute();
    }

    @Test(expected = TestingValidationException.class)
    public void testRequestNotExpectedStatus() throws TestingValidationException {
        client.get("/test")
            .withRetryConditionFactory(ConditionFactoryBuilder.getInstance()
                .withMultiplier(1)
                .withTimeout(Duration.ofSeconds(5))
                .build())
            .withExpectedStatus(HttpStatus.SC_NOT_FOUND)
            .execute();
    }

    @Test(expected = TestingValidationException.class)
    public void testRequestNotExpectedContent() throws TestingValidationException {
        client.get("/test").withExpectedContent("b").execute();
    }

    @Test(expected = TestingValidationException.class)
    public void testRequestNotExpectedCondition() throws TestingValidationException {
        client.get("/test").withExpectedCondition(response -> "b".equals(response.getContent())).execute();
    }

    @Test
    public void testRequestAddParameters() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/test")
            .addParameter("one", "1")
            .addParameter("two", "2")
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestWithParametersList() throws TestingValidationException {
        final List<NameValuePair> parameters = new ArrayList<>();

        parameters.add(new BasicNameValuePair("one", "1"));
        parameters.add(new BasicNameValuePair("two", "2"));

        final SlingHttpResponse response = client.get("/test")
            .withParameters(parameters)
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestWithParametersMap() throws TestingValidationException {
        final Map<String, String> parameters = new LinkedHashMap<>();

        parameters.put("one", "1");
        parameters.put("two", "2");

        final SlingHttpResponse response = client.get("/test")
            .withParameters(parameters)
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestAddHeaders() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/test")
            .addHeader("one", "1")
            .addHeader("two", "2")
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("1", response.getFirstHeader("one").getValue());
        assertEquals("2", response.getFirstHeader("two").getValue());
    }

    @Test
    public void testRequestWithHeaders() throws TestingValidationException {
        final List<Header> headers = new ArrayList<>();

        headers.add(new BasicHeader("one", "1"));
        headers.add(new BasicHeader("two", "2"));

        final SlingHttpResponse response = client.get("/test")
            .withHeaders(headers)
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("1", response.getFirstHeader("one").getValue());
        assertEquals("2", response.getFirstHeader("two").getValue());
    }

    @Test
    public void testRequestDefaultFollowRedirects() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/redirect").execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testRequestFollowRedirects() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/redirect")
            .followRedirects(true)
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testRequestNoFollowRedirects() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/redirect")
            .followRedirects(false)
            .execute();

        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testRequestPostForm() throws TestingValidationException {
        final SlingHttpResponse response = client.post("/form")
            .withForm(FormEntityBuilder.create()
                .addParameter("one", "1")
                .addParameter("two", "2"))
            .execute();

        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestPostFormParameters() throws TestingValidationException {
        final Map<String, String> parameters = new LinkedHashMap<>();

        parameters.put("one", "1");
        parameters.put("two", "2");

        final SlingHttpResponse response = client.post("/form")
            .withFormParameters(parameters)
            .execute();

        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestPostEntity() throws TestingValidationException {
        final SlingHttpResponse response = client.post("/form")
            .withEntity(FormEntityBuilder.create()
                .addParameter("one", "1")
                .addParameter("two", "2")
                .build())
            .execute();

        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
        assertEquals("test?one=1&two=2", response.getContent());
    }

    @Test
    public void testRequestNoRetry() throws TestingValidationException {
        final SlingHttpResponse response = client.get("/noretry")
            .disableRetry()
            .execute();

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("test", response.getContent());
    }

    private static String getResponseBody(final HttpRequest request) throws IOException {
        final StringBuilder builder = new StringBuilder("test");

        final List<NameValuePair> parameters = HttpRequestUtils.getParameters(request);

        if (!parameters.isEmpty()) {
            builder.append("?");
            builder.append(parameters.stream()
                .map(parameter -> parameter.getName() + "=" + parameter.getValue())
                .collect(Collectors.joining("&")));
        }

        return builder.toString();
    }
}
