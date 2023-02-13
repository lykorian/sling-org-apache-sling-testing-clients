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
package org.apache.sling.testing.clients.executor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.exceptions.TestingIOException;
import org.apache.sling.testing.clients.exceptions.TestingValidationException;
import org.apache.sling.testing.clients.executor.builder.ConditionFactoryBuilder;
import org.apache.sling.testing.clients.executor.predicates.SlingHttpResponseBodyContainsPredicate;
import org.apache.sling.testing.clients.executor.predicates.SlingHttpResponseStatusCodePredicate;
import org.apache.sling.testing.clients.executor.resiliency.RetryHelper;
import org.apache.sling.testing.clients.executor.resiliency.VerificationHelper;
import org.apache.sling.testing.clients.executor.verifier.JsonNodeVerifier;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.MultiPartNameValuePair;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builder for retryable HTTP request executions.
 * <p>
 * Note: all checked exceptions are caught and rethrown as runtime exceptions.  These errors should be handled by the
 * JUnit runner and result in test failure.
 */
public final class RequestExecutor {

    /** Value to String conversion function for condition logging. */
    public static final Function<Object, String> DEFAULT_VALUE_TO_STRING_FUNCTION = value -> {
        final String valueAsString;

        if (value instanceof SlingHttpResponse) {
            valueAsString = ((SlingHttpResponse) value).getStatusLine().toString();
        } else {
            valueAsString = value.toString();
        }

        return valueAsString;
    };

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(RequestExecutor.class);

    /** JSON mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Default node depth for JSON requests. */
    private static final int DEFAULT_JSON_DEPTH = 2;

    /** Sling client delegate for request execution. */
    private final SlingClient client;

    /** HTTP request builder to encapsulate HTTP client request properties. */
    private final RequestBuilder delegate;

    /** Condition factory for request retries. */
    private ConditionFactory retryConditionFactory = ConditionFactoryBuilder.getInstance()
        .withValueToStringFunction(true, DEFAULT_VALUE_TO_STRING_FUNCTION)
        .withValueToStringFunction(false, DEFAULT_VALUE_TO_STRING_FUNCTION)
        .build();

    /** Condition factory for request verification. */
    private ConditionFactory verificationConditionFactory = ConditionFactoryBuilder.getInstance()
        .withValueToStringFunction(true, DEFAULT_VALUE_TO_STRING_FUNCTION)
        .withValueToStringFunction(false, DEFAULT_VALUE_TO_STRING_FUNCTION)
        .withDelay(Duration.ofSeconds(10))
        .build();

    /** Expected HTTP status. */
    private int[] expectedStatus = new int[0];

    /** Expected HTTP response body content. */
    private String expectedContent;

    /** Expected HTTP response condition. */
    private Predicate<SlingHttpResponse> expectedCondition;

    /** Verification to perform after request execution. */
    private Callable<Boolean> verifier;

    /** Alias to use for verification requests. */
    private String verifierAlias;

    /** Disable request retries. */
    private boolean disableRetry;

    /** Optional descriptive failure message. */
    private String failureMessage;

    /** If true, stream request without consuming the response entity. */
    private boolean stream;

    /** Alias for condition logging. */
    private String alias;

    /** Request path. */
    private String path;

    /** Built request cache. */
    private HttpUriRequest request;

    /**
     * Create a new request executor.
     *
     * @param client DAM client
     * @param method HTTP method
     */
    public RequestExecutor(@NotNull final SlingClient client, @NotNull final String method) {
        this.client = client;

        delegate = RequestBuilder.create(method);
    }

    /**
     * Set the request path.
     *
     * @param path relative path to resource (path relative to server URL)
     * @return this
     */
    public RequestExecutor path(@NotNull final String path) {
        this.path = path;

        return this;
    }

    /**
     * Add a request parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @return this
     */
    public RequestExecutor addParameter(@NotNull final String name, final String value) {
        delegate.addParameter(name, value);

        return this;
    }

    /**
     * Set the request parameters.
     *
     * @param parameters list of parameter name-value pairs
     * @return this
     */
    public RequestExecutor withParameters(@NotNull final List<NameValuePair> parameters) {
        parameters.forEach(delegate::addParameter);

        return this;
    }

    /**
     * Set the request parameters.
     *
     * @param parameters map of parameter name-value pairs
     * @return this
     */
    public RequestExecutor withParameters(@NotNull final Map<String, String> parameters) {
        parameters.entrySet()
            .stream()
            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
            .forEach(delegate::addParameter);

        return this;
    }

    /**
     * Add a request header.
     *
     * @param name header name
     * @param value header value
     * @return this
     */
    public RequestExecutor addHeader(@NotNull final String name, final String value) {
        delegate.addHeader(name, value);

        return this;
    }

    /**
     * Set the request headers.
     *
     * @param headers list of headers
     * @return this
     */
    public RequestExecutor withHeaders(@NotNull final List<Header> headers) {
        headers.forEach(delegate::addHeader);

        return this;
    }

    /**
     * Set the expected HTTP response status code(s).  An exception will be thrown during execution if the Sling HTTP
     * response does not match this value.
     *
     * @param expectedStatus expected status codes
     * @return this
     */
    public RequestExecutor withExpectedStatus(final int... expectedStatus) {
        this.expectedStatus = expectedStatus;

        return this;
    }

    /**
     * Set the expected content for the HTTP response body.  An exception will be thrown if the response body does not
     * contain the provided value.
     *
     * @param expectedContent response body substring
     * @return this
     */
    public RequestExecutor withExpectedContent(final String expectedContent) {
        this.expectedContent = expectedContent;

        return this;
    }

    /**
     * Set an expected predicate condition for the HTTP response.  An exception will be thrown if the response does not
     * evaluate successfully against the provided predicate.
     *
     * @param expectedCondition response predicate condition
     * @return this
     */
    public RequestExecutor withExpectedCondition(final Predicate<SlingHttpResponse> expectedCondition) {
        this.expectedCondition = expectedCondition;

        return this;
    }

    public RequestExecutor withExpectedJsonNode(final JsonNodeVerifier jsonNodeVerifier) {
        return withExpectedCondition(getJsonNodeVerifierPredicate(jsonNodeVerifier));
    }

    public RequestExecutor withVerifier(@NotNull final Callable<Boolean> verifier, final String alias) {
        this.verifier = verifier;
        this.verifierAlias = alias;

        return this;
    }

    /**
     * Set the redirect behavior.  Following redirects is enabled by default.
     *
     * @param followRedirects true to follow redirects, false to disable following of redirects
     * @return this
     */
    public RequestExecutor followRedirects(final boolean followRedirects) {
        delegate.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
            .setRedirectsEnabled(followRedirects)
            .build());

        return this;
    }

    /**
     * Disable request authentication.
     *
     * @return this
     */
    public RequestExecutor disableAuthentication() {
        delegate.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
            .setAuthenticationEnabled(false)
            .build());

        return this;
    }

    /**
     * Return the HTTP response without consuming the entity.  Caller is responsible for consuming the entity or closing
     * the returned <code>InputStream</code>.  Ignored for <code>JsonNode</code> requests.
     *
     * @return this
     */
    public RequestExecutor stream() {
        stream = true;

        return this;
    }

    /**
     * Set the request charset.
     *
     * @param charset charset
     * @return this
     */
    public RequestExecutor withCharset(@NotNull final Charset charset) {
        delegate.setCharset(charset);

        return this;
    }

    /**
     * Set the request form entity.
     *
     * @param formEntityBuilder form entity builder
     * @return this
     */
    public RequestExecutor withForm(@NotNull final FormEntityBuilder formEntityBuilder) {
        withEntity(formEntityBuilder.build());

        return this;
    }

    /**
     * Set the request form entity with the given parameter map.
     *
     * @param formParameters parameters to use for building form entity
     * @return this
     */
    public RequestExecutor withFormParameters(@NotNull final Map<String, String> formParameters) {
        withForm(FormEntityBuilder.create().addAllParameters(formParameters));

        return this;
    }

    /**
     * Set the request entity.
     *
     * @param entity HTTP entity
     * @return this
     */
    public RequestExecutor withEntity(@NotNull final HttpEntity entity) {
        delegate.setEntity(entity);

        return this;
    }

    /**
     * Set the request entity by serializing the provided object to JSON.
     *
     * @param objectToSerializeAsJson object to serialize
     * @return this
     * @throws JsonProcessingException if object cannot be serialized as JSON
     */
    public RequestExecutor withJsonEntity(@NotNull final Object objectToSerializeAsJson)
        throws JsonProcessingException {
        final String json = MAPPER.writeValueAsString(objectToSerializeAsJson);

        return withEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    }

    /**
     * Set a multipart request entity with the provided parameters.
     *
     * @param parameters parameter map
     * @return this
     */
    public RequestExecutor withMultipartEntity(@NotNull final Map<String, String> parameters) {
        final MultipartEntityBuilder multiPartEntity = MultipartEntityBuilder.create().setLaxMode();

        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            multiPartEntity.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.MULTIPART_FORM_DATA));
        }

        return withEntity(multiPartEntity.build());
    }

    /**
     * Set a multipart request entity with the provided parameters.
     *
     * @param parameters multipart parameter list
     * @return this
     */
    public RequestExecutor withMultipartEntity(@NotNull final List<MultiPartNameValuePair> parameters) {
        final MultipartEntityBuilder multiPartEntity = MultipartEntityBuilder.create()
            .setStrictMode()
            .setBoundary("crxde")
            .setCharset(StandardCharsets.UTF_8);

        for (final MultiPartNameValuePair pair : parameters) {
            final ContentType contentType;

            if (null == pair.getMimetype()) {
                contentType = ContentType.MULTIPART_FORM_DATA;
            } else {
                contentType = ContentType.create(pair.getMimetype(), pair.getCharset());
            }

            multiPartEntity.addPart(pair.getName(), new StringBody(pair.getValue(), contentType));
        }

        return withEntity(multiPartEntity.build());
    }

    /**
     * Override the default HTTP client request config.
     *
     * @param config HTTP client request config
     * @return this
     */
    public RequestExecutor withRequestConfig(@NotNull final RequestConfig config) {
        delegate.setConfig(config);

        return this;
    }

    /**
     * Disable request retries.
     *
     * @return this
     */
    public RequestExecutor disableRetry() {
        this.disableRetry = true;

        return this;
    }

    /**
     * Override the default retry condition factory.
     *
     * @param retryConditionFactory retry condition factory
     * @return this
     */
    public RequestExecutor withRetryConditionFactory(@NotNull final ConditionFactory retryConditionFactory) {
        this.retryConditionFactory = retryConditionFactory;

        return this;
    }

    /**
     * Override the default verification condition factory.
     *
     * @param verificationConditionFactory verification condition factory
     * @return this
     */
    public RequestExecutor withVerificationConditionFactory(@NotNull final ConditionFactory verificationConditionFactory) {
        this.verificationConditionFactory = verificationConditionFactory;

        return this;
    }

    /**
     * Set an alias to use for condition logging.
     *
     * @param alias alias
     * @return this
     */
    public RequestExecutor withAlias(@NotNull final String alias) {
        this.alias = alias;

        return this;
    }

    /**
     * Descriptive failure message to log if request execution fails due to timeout or client exception.
     *
     * @param failureMessage failure message
     * @return this
     */
    public RequestExecutor withFailureMessage(@NotNull final String failureMessage) {
        this.failureMessage = failureMessage;

        return this;
    }

    /**
     * Conditionally execute a request and retry until the expected HTTP response status code is returned
     *
     * @param precondition only execute and verify the request if this evaluates to <code>true</code>
     * @return optional response if request is executed
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public Optional<SlingHttpResponse> execute(final Callable<Boolean> precondition) throws TestingValidationException {
        Optional<SlingHttpResponse> response = Optional.empty();

        try {
            if (precondition.call()) {
                response = Optional.of(execute());
            }
        } catch (Exception e) {
            logAndThrowException(e);
        }

        return response;
    }

    /**
     * Execute a request and retry until the expected HTTP response status code is returned.  Requests are retried using
     * the configured {@link RetryHelper}.
     *
     * @return Sling HTTP response
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public SlingHttpResponse execute() throws TestingValidationException {
        SlingHttpResponse response = null;

        final HttpUriRequest request = getRequest(null);

        if (verifier == null) {
            response = doExecute(request);
        } else {
            try {
                response = getVerificationHelper(request).requestAndVerify(verifierAlias,
                    () -> doExecute(request), verifier);
            } catch (TestingValidationException e) {
                logAndThrowException(e);
            }
        }

        return response;
    }

    /**
     * Execute a request for a path returning a JSON response (e.g. <code>/bin/querybuilder.json</code>).  Unlike the
     * <code>getAsJsonNode</code> methods, this method does not append a depth selector or <code>.json</code> extension
     * to the request path.  This method should be used for requesting servlet paths that return JSON responses, whereas
     * the <code>getJsonNode</code> methods should be used to return the default JSON rendering for a given resource
     * path.
     *
     * @return JSON node
     * @throws TestingIOException if the response body cannot be parsed as a JSON node
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public JsonNode getJsonNode() throws TestingIOException, TestingValidationException {
        return getJsonNodeFromResponseBody(execute());
    }

    /**
     * Execute a request for a path returning a JSON response (e.g. <code>/bin/querybuilder.json</code>).  Unlike the
     * <code>getAsJsonNode</code> methods, this method does not append a depth selector or <code>.json</code>
     * extension to the request path.  This method should be used for requesting servlet paths that return JSON
     * responses, whereas the <code>getJsonNode</code> methods should be used to return the default JSON rendering for a
     * given resource path.  The JSON response will then be mapped using the provided type to return a deserialized Java
     * object.
     *
     * @param type type to map JSON response
     * @param <T> type
     * @return object from deserializing JSON response
     * @throws TestingIOException if the response body cannot be parsed as a JSON node
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public <T> T getJsonNodeAsType(final Class<T> type) throws TestingIOException, TestingValidationException {
        return getJsonNodeAsType(execute(), type);
    }

    /**
     * Execute a request for a JSON node with the default depth and retry until the response matches the expected HTTP
     * status code and the node exists. Requests are retried using the configured {@link RetryHelper} for GET, HEAD,
     * OPTIONS, and TRACE requests.
     *
     * @return JSON node
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public JsonNode getAsJsonNode() throws TestingValidationException {
        return getAsJsonNode(DEFAULT_JSON_DEPTH);
    }

    /**
     * Execute a request for a JSON node with the given depth and retry until the response matches the expected HTTP
     * status code and the node exists. Requests are retried using the configured {@link RetryHelper}.
     *
     * @param depth JSON node depth
     * @return JSON node
     * @throws TestingValidationException if request cannot be executed or does not meet expected conditions
     */
    public JsonNode getAsJsonNode(final int depth) throws TestingValidationException {
        JsonNode jsonNode = null;

        try {
            final String extension = getJsonExtension(depth);
            final HttpUriRequest request = getRequest(extension);

            final SlingHttpResponse response;

            if (disableRetry) {
                response = doExecute(request);
            } else {
                response = getRetryHelper(request).retryUntilCondition(() -> doExecute(request), getPredicate());
            }

            jsonNode = getJsonNodeFromResponseBody(response);
        } catch (ConditionTimeoutException | ClientException e) {
            logAndThrowException(e);
        }

        return jsonNode;
    }

    // internals

    private SlingHttpResponse doExecute(final HttpUriRequest request) throws TestingValidationException {
        SlingHttpResponse response = null;

        try {
            if (disableRetry) {
                response = doRequest(request);

                if (!getPredicate().test(response)) {
                    throw new TestingValidationException("HTTP response body does not meet expected condition");
                }
            } else {
                response = getRetryHelper(request).retryUntilCondition(() -> doRequest(request), getPredicate());
            }
        } catch (ConditionTimeoutException | ClientException e) {
            logAndThrowException(e);
        }

        return response;
    }

    private RetryHelper getRetryHelper(final HttpUriRequest request) {
        final String alias = Optional.ofNullable(this.alias)
            .orElse(getDefaultAlias(request));

        return new RetryHelper(retryConditionFactory.alias(alias));
    }

    private VerificationHelper getVerificationHelper(final HttpUriRequest request) {
        final String alias = Optional.ofNullable(this.alias)
            .orElse(getDefaultAlias(request));

        return new VerificationHelper(verificationConditionFactory.alias(alias));
    }

    private String getDefaultAlias(final HttpUriRequest request) {
        return new StringBuilder(request.getMethod())
            .append(" ")
            .append(request.getURI().getPath())
            .toString();
    }

    /**
     * Get the Sling HTTP response predicate based on the expected conditions.
     *
     * @return predicate for evaluating the HTTP response
     */
    private Predicate<SlingHttpResponse> getPredicate() {
        Predicate<SlingHttpResponse> predicate = new SlingHttpResponseStatusCodePredicate(expectedStatus);

        if (expectedContent != null) {
            predicate = predicate.and(new SlingHttpResponseBodyContainsPredicate(expectedContent));
        }

        if (expectedCondition != null) {
            predicate = predicate.and(expectedCondition);
        }

        return predicate;
    }

    /**
     * Log optional failure message and rethrow exception.
     *
     * @param exception exception to be logged and rethrown
     */
    private void logAndThrowException(final Exception exception) throws TestingValidationException {
        if (failureMessage == null) {
            throw new TestingValidationException("error executing request", exception);
        } else {
            throw new TestingValidationException(failureMessage, exception);
        }
    }

    private SlingHttpResponse doRequest(final HttpUriRequest request) throws ClientException {
        final SlingHttpResponse response;

        if (stream) {
            response = client.doStreamRequest(request, null, expectedStatus);
        } else {
            response = client.doRequest(request, null, expectedStatus);
        }

        return response;
    }

    /**
     * Build the HTTP request to be executed.
     *
     * @param extension optional extension, may be null
     * @return HTTP request
     */
    private HttpUriRequest getRequest(final String extension) {
        if (request == null) {
            request = delegate
                .setUri(client.getUrl(path + StringUtils.trimToEmpty(extension)))
                .build();

            LOG.debug("HTTP request: {}", request);
        }

        return request;
    }

    /**
     * Get a predicate condition for the provided JSON node verifier.
     *
     * @param jsonNodeVerifier JSON node verifier
     * @return predicate that will evaluate to true when the JSON node is verified
     */
    private Predicate<SlingHttpResponse> getJsonNodeVerifierPredicate(final JsonNodeVerifier jsonNodeVerifier) {
        return response -> {
            try {
                return jsonNodeVerifier.verify(getJsonNodeFromResponseBody(response));
            } catch (TestingIOException e) {
                LOG.info("error getting response as JSON node: {}", e.getMessage());

                return false;
            }
        };
    }

    /**
     * Get a JSON node from the Sling HTTP response body.
     *
     * @param response Sling HTTP response
     * @return JSON node
     * @throws TestingIOException if response body cannot be parsed as a JSON node
     */
    private JsonNode getJsonNodeFromResponseBody(final SlingHttpResponse response) throws TestingIOException {
        return JsonUtils.getJsonNodeFromString(response.getContent());
    }

    /**
     * Get a JSON node deserialized as the given type from the Sling HTTP response body.
     *
     * @param response Sling HTTP response
     * @param type type to map JSON node
     * @param <T> object type
     * @return instance of type deserialized from JSON
     * @throws TestingIOException if JSON cannot be read and/or deserialized as type
     */
    private <T> T getJsonNodeAsType(final SlingHttpResponse response, final Class<T> type) throws TestingIOException {
        try {
            return MAPPER.readValue(response.getContent(), type);
        } catch (JsonProcessingException e) {
            throw new TestingIOException("error reading JSON node as type: " + type, e);
        }
    }

    private String getJsonExtension(final int depth) {
        final String extension;

        if (depth == -1) {
            extension = ".infinity.json";
        } else {
            extension = "." + depth + ".json";
        }

        return extension;
    }
}
