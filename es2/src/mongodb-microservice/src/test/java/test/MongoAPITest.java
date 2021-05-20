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

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class MongoAPITest {

    private static final Logger logger = LoggerFactory.getLogger(test.MongoAPITest.class);

    private static HttpRequest<Buffer> request;
    private static Vertx vertx;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();

    }

    @Test
    public void saveUserAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mongo-api/saveUserAPI";
        JsonObject body = new JsonObject()
                .put("id", UUID.randomUUID().toString().replace("-", ""))
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing saveUser API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void getAllUsersFromIdAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mongo-api/1";

        logger.info("Testing getAllUsersFromId API...");

        sendPostRequest(apiPath, new JsonObject(), future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
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
