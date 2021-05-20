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
import static org.junit.Assert.assertNotEquals;

public class MysqlAPITest {

    private static final Logger logger = LoggerFactory.getLogger(test.MysqlAPITest.class);

    private static HttpRequest<Buffer> request;
    private static Vertx vertx;
    private static String user;

    @Before
    public void setUpUnit() {
        vertx = Vertx.vertx();
        user = Double.toString(Math.random());
    }

    @Test
    public void saveUserAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/userAPI";
        JsonObject body = new JsonObject()
                .put("id", UUID.randomUUID().toString().replace("-", ""))
                .put("name", "test")
                .put("username", "test" + Math.random())
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing saveUser API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 201 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(201), Optional.of(future.get()));
    }

    @Test
    public void getLastUserAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/getLastUserId";

        logger.info("Testing getLastUser API...");

        sendGetRequest(apiPath, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void getUserByIdAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/getUserById/1";

        logger.info("Testing getUserById API...");

        sendGetRequest(apiPath, future);

        logger.info("EXPECTED: 404 \t" + "RESULT: " + future.get());

        assertNotEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void getUserByUsernameAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture<Integer> futureUser = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/user/username";
        String apiPathAddUser = "/api/mysql-api/userAPI";
        JsonObject body = new JsonObject().put("username", "test" + user);
        JsonObject bodyUser = new JsonObject()
                .put("id", UUID.randomUUID().toString().replace("-", ""))
                .put("name", "test")
                .put("username", "test" + user)
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Adding the user whit a target userneame to find..");

        sendPostRequest(apiPathAddUser, bodyUser, futureUser);

        assertEquals(Optional.of(201), Optional.of(futureUser.get()));

        logger.info("Testing getUserByUsername API...");

        sendPostRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void getAllUsersAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/user";

        logger.info("Testing getAllUsers API...");

        sendGetRequest(apiPath, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }

    @Test
    public void updateUserAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/updateUserAPI";
        JsonObject body = new JsonObject()
                .put("name", "testUpdate")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing updateUser API...");

        sendPutRequest(apiPath, body, future);

        logger.info("EXPECTED: 200 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(200), Optional.of(future.get()));
    }


    @Test
    public void deleteUserAPITest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String apiPath = "/api/mysql-api/user/username";
        JsonObject body = new JsonObject().put("username", "test");

        logger.info("Testing deleteUser API...");

        sendDeleteRequest(apiPath, body, future);

        logger.info("EXPECTED: 204 \t" + "RESULT: " + future.get());

        assertEquals(Optional.of(204), Optional.of(future.get()));
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

    private void sendDeleteRequest(String apiPath, JsonObject body, CompletableFuture<Integer> resultHandler) {
        request = WebClient.create(vertx)
                .delete(8080, "localhost", apiPath)
                .putHeader("Content-Type", "application/json");

        request.sendJson(body, ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().statusCode());
            }else resultHandler.complete(ar.result().statusCode());
        });
    }

    private void sendPutRequest(String apiPath, JsonObject body, CompletableFuture<Integer> resultHandler) {
        request = WebClient.create(vertx)
                .put(8080, "localhost", apiPath)
                .putHeader("Content-Type", "application/json");

        request.sendJson(body, ar -> {
            if (ar.succeeded()){
                resultHandler.complete(ar.result().statusCode());
            }else resultHandler.complete(ar.result().statusCode());
        });
    }
}
