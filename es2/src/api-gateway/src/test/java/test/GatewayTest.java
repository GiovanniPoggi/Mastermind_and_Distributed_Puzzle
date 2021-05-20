package test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class GatewayTest {

    private static final Logger logger = LoggerFactory.getLogger(GatewayTest.class);

    private static HttpRequest<Buffer> request;
    private static Vertx vertx;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();
    }

    @Test
    public void testGatewayCorrectRedirect() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPathAddUser = "/api/auth-api/auth/signup";
        JsonObject body = new JsonObject()
                .put("name", "test" + Math.random())
                .put("username", "test" + Math.random())
                .put("email", "test@gmail.com")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing Gateway redirection given a correct path..");

        sendPostRequest(apiPathAddUser, body, future);
        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());
        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void testGatewayWrongRedirect() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/test/addOnlineUser/1";
        JsonObject body = new JsonObject().put("username", "test");

        logger.info("Testing Gateway redirection given a wrong path..");

        sendPostRequest(apiPath, body, future);
        logger.info("EXPECTED: 404 \t" + "RESULT: " + future.get());
        assertEquals(Optional.of(404), Optional.of(future.get()));
    }

    private void sendPostRequest(String apiPath, JsonObject body, CompletableFuture<Integer> resultHandler) {
        request = WebClient.create(vertx)
                .post(8080, "localhost", apiPath)
                .putHeader("Content-Type", "application/json");

        request.sendJson(body, ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().statusCode());
            }else resultHandler.complete(ar.result().statusCode());
        });
    }
}
