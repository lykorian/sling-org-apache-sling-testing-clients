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
