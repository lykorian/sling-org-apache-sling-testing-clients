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
package org.apache.sling.testing.clients;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class HttpLoggingConfig {

    public static final String LOGGING_PROP_PREFIX = "http.logging.";

    public static final String ENABLED_PROP = "enabled";

    public static final String LOG_REQUEST_ENTITY_PROP = "logRequestEntity";

    public static final String LOG_RESPONSE_ENTITY_PROP = "logResponseEntity";

    public static final String LOG_REQUEST_HEADERS_PROP = "logRequestHeaders";

    public static final String LOG_RESPONSE_HEADERS_PROP = "logResponseHeaders";

    public static final String EXCLUDED_HEADERS = "excludedHeaders";

    public boolean isEnabled() {
        return Boolean.getBoolean(getPrefixedPropertyName(ENABLED_PROP));
    }

    public boolean isLogRequestEntity() {
        return Boolean.getBoolean(getPrefixedPropertyName(LOG_REQUEST_ENTITY_PROP));
    }

    public boolean isLogResponseEntity() {
        return Boolean.getBoolean(getPrefixedPropertyName(LOG_RESPONSE_ENTITY_PROP));
    }

    public boolean isLogRequestHeaders() {
        return Boolean.getBoolean(getPrefixedPropertyName(LOG_REQUEST_HEADERS_PROP));
    }

    public boolean isLogResponseHeaders() {
        return Boolean.getBoolean(getPrefixedPropertyName(LOG_RESPONSE_HEADERS_PROP));
    }

    public Set<String> getExcludedHeaders() {
        final String excludedHeaders = System.getProperty(getPrefixedPropertyName(EXCLUDED_HEADERS), "");

        return new HashSet<>(Arrays.asList(excludedHeaders.split(",")));
    }

    private String getPrefixedPropertyName(final String prop) {
        return SystemPropertiesConfig.CONFIG_PROP_PREFIX + LOGGING_PROP_PREFIX + prop;
    }
}
