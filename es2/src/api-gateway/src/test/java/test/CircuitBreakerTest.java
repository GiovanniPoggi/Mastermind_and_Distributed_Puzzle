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

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class CircuitBreakerTest {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerTest.class);

    private static HttpRequest<Buffer> request;
    private static HttpRequest<Buffer> requestTest;
    private static Vertx vertx;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();
    }
    @Test
    public void testCircuitBreaker() throws ExecutionException, InterruptedException {
        ArrayList<CompletableFuture<Integer>> futureList = new ArrayList<>();
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/puzzle-api/fail";
        String apiPathAddUser = "/api/auth-api/auth/signup";
        JsonObject body = new JsonObject()
                .put("name", "test" + Math.random())
                .put("username", "test" + Math.random())
                .put("email", "test@gmail.com")
                .put("password", "test")
                .put("role", "test");

        JsonObject body2 = new JsonObject()
                .put("name", "test" + Math.random())
                .put("username", "test" + Math.random())
                .put("email", "test@gmail.com")
                .put("password", "test")
                .put("role", "test");

        requestTest = WebClient.create(vertx)
                .post(8080, "localhost", apiPathAddUser)
                .putHeader("Content-Type", "application/json");

        logger.info("Testing circuit breaker proper working..");

        logger.info("Sending a correct request to a working microservice..");

        sendPostRequest(apiPathAddUser, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));

        logger.info("Triggering circuit closure..");
        for(int i = 0; i < 20; i++){
            CompletableFuture<Integer> fut = new CompletableFuture<>();
            futureList.add(fut);
            sendPostRequest(apiPath, new JsonObject(), fut);
            assertEquals(Optional.of(502), Optional.of(fut.get()));
        }

        logger.info("Sending a request while circuit is closed..");
        requestTest.sendJson(body, ar -> {
            if(ar.succeeded()){
                logger.info("EXPECTED: 502 \t" + "RESULT: " + ar.result().statusCode());
                assertEquals(Optional.of(502), Optional.of(ar.result().statusCode()));
            }
        });

        logger.info("Waiting for circuit to reopen..");
        Thread.sleep(10000);

        logger.info("sending a request after the circuit reopened..");
        requestTest.sendJson(body2, ar -> {
            if(ar.succeeded()){
                logger.info("EXPECTED: 200 \t" + "RESULT: " + ar.result().statusCode());
                assertEquals(Optional.of(200), Optional.of(ar.result().statusCode()));
            }
        });
        Thread.sleep(1000);

    }

    private void sendGetRequest(String apiPath, CompletableFuture<Integer> resultHandler) {
        request = WebClient.create(vertx)
                .get(8080, "localhost", apiPath)
                .putHeader("Content-Type", "application/json");

        request.send(ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().statusCode());
            }else resultHandler.complete(ar.result().statusCode());
        });
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
