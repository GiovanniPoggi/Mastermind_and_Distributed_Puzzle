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

public class PuzzleAPITest {
    private static final Logger logger = LoggerFactory.getLogger(test.PuzzleAPITest.class);

    private static HttpRequest<Buffer> request;
    private static Vertx vertx;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();

    }

    @Test
    public void playPuzzleAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/puzzle-api/play";
        JsonObject body = new JsonObject()
                .put("dimensionCols", "4")
                .put("imgUrl", "https://upload.wikimedia.org/wikipedia/commons/6/6e/Bletchley_Park.jpg")
                .put("username", "test");

        logger.info("Testing playPuzzle API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void joinPuzzleAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/puzzle-api/join";
        JsonObject body = new JsonObject()
                .put("username", "test");

        logger.info("Testing join puzzle API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void swapTailsAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/puzzle-api/swap";
        JsonObject body = new JsonObject()
                .put("position0", "2")
                .put("position1", "3")
                .put("username", "test");

        logger.info("Testing swap tiles API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void messageStressAPITest() {
        ArrayList<CompletableFuture<Integer>> futList = new ArrayList<>();
        ArrayList<Integer> responses = new ArrayList<>();
        String apiPath = "/api/puzzle-api/swap";
        JsonObject body = new JsonObject()
                .put("position0", "2")
                .put("position1", "3")
                .put("username", "test");

        logger.info("Making a stress test to swap tiles API...");

        for(int i = 0 ; i  < 100 ; i++){
            futList.add(new CompletableFuture<>());
        }

        futList.forEach(future -> {
            sendPostRequest(apiPath, body, future);

            try {
                responses.add(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        logger.info("EXPECTED: 0 \t" + "RESULT: " + responses.stream().filter(b -> !b.equals(200)).count());

        assertEquals(0, responses.stream().filter(b -> !b.equals(200)).count());
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
