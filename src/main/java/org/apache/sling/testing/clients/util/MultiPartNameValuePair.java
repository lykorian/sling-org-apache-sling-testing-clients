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
