package org.apache.sling.testing.clients.resiliency;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.executor.RequestExecutor;

/**
 * Client providing resilient request execution with configurable retry/polling behavior.
 */
public class ResilientSlingClient extends SlingClient {

    /**
     * Constructor used by adaptTo.
     *
     * @param http underlying HttpClient
     * @param config config state
     * @throws ClientException if the client cannot be created
     */
    public ResilientSlingClient(final CloseableHttpClient http, final SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    /**
     * Convenience constructor.
     *
     * @param url host url
     * @param user username
     * @param password password
     * @throws ClientException if the client cannot be created
     */
    public ResilientSlingClient(final URI url, final String user, final String password) throws ClientException {
        super(url, user, password);
    }

    /**
     * Execute a <code>GET</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor get(final String path) {
        return request(HttpGet.METHOD_NAME, path);
    }

    /**
     * Execute a <code>POST</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor post(final String path) {
        return request(HttpPost.METHOD_NAME, path);
    }

    /**
     * Execute a <code>PATCH</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor patch(final String path) {
        return request(HttpPatch.METHOD_NAME, path);
    }

    /**
     * Execute a <code>PUT</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor put(final String path) {
        return request(HttpPut.METHOD_NAME, path);
    }

    /**
     * Execute a <code>HEAD</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor head(final String path) {
        return request(HttpHead.METHOD_NAME, path);
    }

    /**
     * Execute a <code>DELETE</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor delete(final String path) {
        return request(HttpDelete.METHOD_NAME, path);
    }

    /**
     * Execute a <code>OPTIONS</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor options(final String path) {
        return request(HttpOptions.METHOD_NAME, path);
    }

    /**
     * Execute a <code>TRACE</code> request.
     *
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor trace(final String path) {
        return request(HttpTrace.METHOD_NAME, path);
    }

    /**
     * Execute a request.
     *
     * @param method request method
     * @param path request path
     * @return request executor for building and executing request
     */
    public RequestExecutor request(final String method, final String path) {
        return new RequestExecutor(this, method).path(path);
    }
}
