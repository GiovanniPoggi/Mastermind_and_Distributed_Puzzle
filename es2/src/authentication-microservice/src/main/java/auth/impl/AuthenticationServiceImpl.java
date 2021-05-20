package auth.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import auth.AuthenticationService;
import auth.JsonWebToken;
import sd.microservices.mysql.AccountService;

import java.util.UUID;

public class AuthenticationServiceImpl implements AuthenticationService {

    private JsonWebToken jwt;
    private ServiceDiscovery discovery;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    /**
     *
     * @return randomUUID String
     */
    private String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     *
     * @param discovery Service Discovery instance
     */
    public AuthenticationServiceImpl(ServiceDiscovery discovery){
        this.jwt = new JsonWebToken();
        this.discovery = discovery;
    }


    /**
     *  Create token for user login from username and password
     *
     * @param user
     * @param resultHandler
     */
    @Override
    public void createToken(JsonObject user, Handler<AsyncResult<String>> resultHandler) {
        String username = user.getString("username");
        Promise<String> promise = Promise.promise();
        EventBusService.getProxy(discovery, AccountService.class, ar -> {
            if (ar.succeeded()) {
                AccountService service = ar.result();
                service.getByUsername(username,handler -> {
                    if(handler.succeeded()) {
                        String pwdUser = jwt.hashPwd(user.getString("password"));
                        if(pwdUser.equals(handler.result().getPassword())){
                            JsonObject account = new JsonObject()
                                    .put("id", handler.result().getId())
                                    .put("name", handler.result().getName())
                                    .put("username", handler.result().getUsername())
                                    .put("email", handler.result().getEmail())
                                    .put("password", handler.result().getPassword())
                                    .put("role", handler.result().getRole());

                            String token = jwt.createToken(account);
                            logger.info("TOKEN: " + token);
                            resultHandler.handle(Future.succeededFuture(token));
                        } else {
                            resultHandler.handle(Future.failedFuture("Invalid password"));
                        }
                    } else {
                        resultHandler.handle(Future.failedFuture("Invalid credential"));
                    }
                });

            } else {
                promise.fail(ar.cause());
            }
        });

    }

    /**
     *  Check if the input token is valid
     *
     * @param token
     * @param resultHandler
     */
    @Override
    public void verifyToken(String token, Handler<AsyncResult<Void>> resultHandler) {
        if(jwt.verifyToken(token)){
            resultHandler.handle(Future.succeededFuture());
        } else {
            resultHandler.handle(Future.failedFuture("Invalid token!"));
        }

    }

    /**
     *  Return String of user's role from input token
     *
     * @param token
     * @param resultHandler
     */
    @Override
    public void getRole(String token, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(jwt.getRole(token)));
    }

    /**
     *  Store user credential
     *
     * @param user
     * @param resultHandler
     */
    @Override
    public void signUp(JsonObject user, Handler<AsyncResult<String>> resultHandler) {
        String pwd = jwt.hashPwd(user.getString("password"));
        JsonArray params = new JsonArray().add(createUUID())
                .add(user.getString("name"))
                .add(user.getString("username"))
                .add(user.getString("email"))
                .add(pwd)
                .add(user.getString("role"));
        Promise<String> promise = Promise.promise();
        EventBusService.getProxy(discovery, AccountService.class, ar -> {
            if (ar.succeeded()) {
                AccountService service = ar.result();
                service.addUserProxy(params,handler -> {
                    if(handler.succeeded()) {
                        resultHandler.handle(Future.succeededFuture("User created"));
                    } else {
                        resultHandler.handle(Future.failedFuture(handler.cause()));
                    }
                });
            } else {
                promise.fail(ar.cause());
            }
        });
    }
}
