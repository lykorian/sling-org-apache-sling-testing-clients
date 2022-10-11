package org.apache.sling.testing.clients.executor;

import org.apache.http.entity.StringEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.HttpServerRule;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.ClassRule;
import org.junit.Test;

public class RequestExecutorTest {

    private static final String POST_PATH = "/test/a/b/c";

    private static final String HTML_RESPONSE = "<div id=\"Path\">/a/b/c</div>";

    @ClassRule
    public static HttpServerRule httpServer = new HttpServerRule() {
        @Override
        protected void registerHandlers() {
            serverBootstrap.registerHandler(POST_PATH, (request, response, context) ->
                response.setEntity(new StringEntity(HTML_RESPONSE)));
        }
    };

    @Test
    public void testBuildHttpRequest() throws ClientException {
        final SlingClient client = new SlingClient(httpServer.getURI(), "user", "pass");

        final RequestExecutor requestExecutor = new RequestExecutor(client);

        requestExecutor.get("");
    }
}
