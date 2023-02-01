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
package org.apache.sling.testing.util;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * HTTP request utility methods.
 */
public final class HttpRequestUtils {

    /**
     * Get the list of parameters for a request.
     *
     * @param request HTTP request
     * @return list of parameters or empty list
     * @throws IOException if error occurs parsing the HTTP request entity
     */
    public static List<NameValuePair> getParameters(final HttpRequest request) throws IOException {
        final List<NameValuePair> parameters;

        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

            parameters = URLEncodedUtils.parse(entity);
        } else {
            final URI uri = URI.create(request.getRequestLine().getUri());

            parameters = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        }

        return parameters;
    }

    /**
     * Deny instantiation.
     */
    private HttpRequestUtils() {

    }
}
