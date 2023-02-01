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
package org.apache.sling.testing.clients.interceptors;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.sling.testing.clients.SystemPropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP client interceptor for logging request/response details.
 */
public final class LoggingInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingInterceptor.class);

    /** Headers to exclude from logging. */
    private static final Set<String> DEFAULT_EXCLUDED_HEADERS = new HashSet<>(Collections.singletonList("Cookie"));

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws IOException {
        if (LOG.isInfoEnabled()) {
            final HttpRequest request = HttpClientContext.adapt(context).getRequest();
            final int statusCode = response.getStatusLine().getStatusCode();

            final StringBuilder logBuilder = new StringBuilder();

            logBuilder.append(request.getRequestLine().toString());
            logBuilder.append(", status code: ");
            logBuilder.append(statusCode);

            if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
                logBuilder.append(", reason: ");
                logBuilder.append(response.getStatusLine().getReasonPhrase());
            }

            if (SystemPropertiesConfig.getHttpLoggingConfig().isLogRequestHeaders()) {
                appendHeaders(logBuilder, request, "request");
            }

            if (SystemPropertiesConfig.getHttpLoggingConfig().isLogRequestEntity()) {
                appendEntity(logBuilder, getRequestEntity(request), "request");
            }

            if (SystemPropertiesConfig.getHttpLoggingConfig().isLogResponseHeaders()) {
                appendHeaders(logBuilder, request, "response");
            }

            if (SystemPropertiesConfig.getHttpLoggingConfig().isLogResponseEntity()) {
                appendEntity(logBuilder, response.getEntity(), "response");
            }

            LOG.info(logBuilder.toString());
        }
    }

    private void appendHeaders(final StringBuilder logBuilder, final HttpMessage message, final String label) {
        logBuilder.append(", ").append(label).append(" headers: ");
        logBuilder.append(getHeaderMap(message.getAllHeaders()));
    }

    private void appendEntity(final StringBuilder logBuilder, final HttpEntity entity, final String label)
        throws IOException {
        if (entity != null) {
            logBuilder.append(", ").append(label).append(" entity: ");
            logBuilder.append(entity);

            if (entity.isRepeatable()) {
                logBuilder.append(", ").append(label).append(" entity as string: ");
                logBuilder.append(EntityUtils.toString(entity));
            }
        }
    }

    private HttpEntity getRequestEntity(final HttpRequest request) {
        HttpEntity entity = null;

        if (request instanceof HttpEntityEnclosingRequest) {
            entity = ((HttpEntityEnclosingRequest) request).getEntity();
        }

        return entity;
    }

    private Map<String, String> getHeaderMap(final Header[] headers) {
        final Set<String> configExcludedHeaders = SystemPropertiesConfig.getHttpLoggingConfig().getExcludedHeaders();
        final Set<String> excludedHeaders = configExcludedHeaders.isEmpty() ? DEFAULT_EXCLUDED_HEADERS :
            configExcludedHeaders;

        return Arrays.stream(headers)
            .filter(header -> !excludedHeaders.contains(header.getName()))
            .collect(Collectors.toMap(Header::getName, Header::getValue));
    }
}
