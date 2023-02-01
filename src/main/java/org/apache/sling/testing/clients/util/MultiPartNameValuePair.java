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
package org.apache.sling.testing.clients.util;

import java.nio.charset.Charset;

import org.apache.http.message.BasicNameValuePair;

public final class MultiPartNameValuePair extends BasicNameValuePair {

    private String mimeType;

    private String charset;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    public MultiPartNameValuePair(final String name, final String value) {
        super(name, value);
    }

    /**
     * Generates a customized NameValuePair for multipart entities.
     *
     * @param name name
     * @param value value
     * @param mimeType MIME type
     * @param charset charset
     */
    public MultiPartNameValuePair(final String name, final String value, final String mimeType, final String charset) {
        super(name, value);
        this.mimeType = mimeType;
        this.charset = charset;
    }

    /**
     * @return the MIME type
     */
    public String getMimetype() {
        return mimeType;
    }

    /**
     * @return the charset
     */
    public Charset getCharset() {
        return Charset.forName(charset);
    }
}
