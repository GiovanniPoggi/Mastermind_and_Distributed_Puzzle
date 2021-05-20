package test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.Before;
import org.junit.Test;
import auth.AuthenticationService;
import auth.JsonWebToken;
import auth.impl.AuthenticationServiceImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class AuthTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthTest.class);

    private static JsonWebToken jsonWebToken;
    private static AuthenticationService authenticationService;

    @Before
    public void setUpUnit() {
        Vertx vertx = Vertx.vertx();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions());
        jsonWebToken = new JsonWebToken();
        authenticationService = new AuthenticationServiceImpl(discovery);
    }

    @Test
    public void createTokenTest(){
        JsonObject account = new JsonObject()
                .put("id", "test")
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "user");

        assertNotNull(jsonWebToken.createToken(account));
    }

    @Test
    public void verifyTokenTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        JsonObject account = new JsonObject()
                .put("id", "test")
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "user");

        logger.info("Testing verifyToken method..");

        authenticationService.verifyToken(jsonWebToken.createToken(account), ar -> future.complete(ar.succeeded()));

        logger.info("EXPECTED: true \t" + "RESULT: " + future.get());

        assertTrue(future.get());
    }

    @Test
    public void verifyRoleUserTest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        JsonObject account = new JsonObject()
                .put("id", "test")
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "user");

        logger.info("Testing verifyRole method..");

        authenticationService.getRole(jsonWebToken.createToken(account) , ar -> future.complete(ar.result()));

        logger.info("EXPECTED: user \t" + "RESULT: " + future.get());

        assertEquals("user", future.get());
    }

    @Test
    public void verifyRoleAdminTest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        JsonObject account = new JsonObject()
                .put("id", "test")
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "admin");

        logger.info("Testing verifyRole method..");

        authenticationService.getRole(jsonWebToken.createToken(account) , ar -> future.complete(ar.result()));

        logger.info("EXPECTED: admin \t" + "RESULT: " + future.get());

        assertEquals("admin", future.get());
    }

    @Test
    public void verifyUnexpectedRoleTest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        JsonObject account = new JsonObject()
                .put("id", "test")
                .put("name", "test")
                .put("username", "test")
                .put("email", "test")
                .put("password", "test")
                .put("role", "test");

        logger.info("Testing verifyRole method..");

        authenticationService.getRole(jsonWebToken.createToken(account) , ar -> future.complete(ar.result()));

        logger.info("EXPECTED: test \t" + "RESULT: " + future.get());

        assertNotEquals("user", future.get());
    }
}
