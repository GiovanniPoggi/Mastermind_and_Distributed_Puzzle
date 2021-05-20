package test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthAPITest {
    private static final Logger logger = LoggerFactory.getLogger(AuthAPITest.class);

    private static HttpRequest<Buffer> request;
    private static Vertx vertx;
    private static String username;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();
        username = "testAuth";
        registerTestUser();
    }

    @Test
    public void aSignUPAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/auth-api/auth/signup";
        JsonObject body = new JsonObject()
                .put("name", "test")
                .put("username", UUID.randomUUID().toString().replace("-", ""))
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing SignUp API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());
        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void signINAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/auth-api/login";
        JsonObject body = new JsonObject()
                .put("username", username)
                .put("password", "test");

        logger.info("Testing signIN API...");

        sendPostRequest(apiPath, body, future);
        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());
        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void getRoleAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<String> token = new CompletableFuture<>();
        String authApiPath = "/api/auth-api/login";
        String roleApiPath = "/api/auth-api/auth/role";
        JsonObject body = new JsonObject()
                .put("username", username)
                .put("password", "test");

        logger.info("Testing getRole API...");

        sendPostRequestString(authApiPath, body, token);

        sendPostWhitAuthorizationRequestString(token.get(), roleApiPath, future);
        logger.info("EXPECTED: test \t" + "RESULT: " + future.get());
        assertEquals("test", future.get());
    }

    @Test
    public void verifyTokenAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture<String> token = new CompletableFuture<>();
        String authApiPath = "/api/auth-api/login";
        String tokenApiPath = "/api/auth-api/auth/token";
        JsonObject body = new JsonObject()
                .put("username", username)
                .put("password", "test");

        logger.info("Testing verifyToken API...");

        sendPostRequestString(authApiPath, body, token);

        sendPostWhitAuthorizationRequestInteger(token.get(), tokenApiPath, future);
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

    private void sendPostRequestString(String apiPath, JsonObject body, CompletableFuture<String> resultHandler) {
        request = WebClient.create(vertx)
                .post(8080, "localhost", apiPath)
                .putHeader("Content-Type", "application/json");

        request.sendJson(body, ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().bodyAsString());
            }else resultHandler.complete(ar.cause().toString());
        });
    }

    private void sendPostWhitAuthorizationRequestString(String token, String apiPath, CompletableFuture<String> resultHandler) {
        request = WebClient.create(vertx)
                .post(8080, "localhost", apiPath)
                .putHeader("Authorization", token);

        request.send(ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().bodyAsString());
            }else resultHandler.complete(ar.result().bodyAsString());
        });
    }

    private void sendPostWhitAuthorizationRequestInteger(String token, String apiPath, CompletableFuture<Integer> resultHandler) {
        request = WebClient.create(vertx)
                .post(8080, "localhost", apiPath)
                .putHeader("Authorization", token);

        request.send(ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().statusCode());
            }else resultHandler.complete(ar.result().statusCode());
        });
    }

    private void registerTestUser(){
        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        String apiPath = "/api/auth-api/auth/signup";
        JsonObject body2 = new JsonObject()
                .put("name", "test")
                .put("username", username)
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");
        sendPostRequest(apiPath, body2, future2);
    }
}
