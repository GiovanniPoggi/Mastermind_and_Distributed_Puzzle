package auth;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface AuthenticationService {
    /**
     * The name of the event bus service.
     */
    String SERVICE_NAME = "authentication-eb-service";

    /**
     * The address on which the service is published.
     */
    String SERVICE_ADDRESS = "service.authentication";

    /**
     *  Create token for user login from username and password
     *
     * @param user
     * @param resultHandler
     */
    void createToken(JsonObject user, Handler<AsyncResult<String>> resultHandler);

    /**
     *  Check if the input token is valid
     *
     * @param token
     * @param resultHandler
     */
    void verifyToken(String token, Handler<AsyncResult<Void>> resultHandler);

    /**
     *  Return String of user's role from input token
     *
     * @param token
     * @param resultHandler
     */
    void getRole(String token, Handler<AsyncResult<String>> resultHandler);

    /**
     *  Store user credential
     *
     * @param user
     * @param resultHandler
     */
    void signUp(JsonObject user, Handler<AsyncResult<String>> resultHandler);

}
