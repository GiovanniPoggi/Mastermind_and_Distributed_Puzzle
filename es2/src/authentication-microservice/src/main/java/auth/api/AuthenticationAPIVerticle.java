package auth.api;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import auth.AuthenticationService;
import sd.microservices.common.RestAPIVerticle;

public class AuthenticationAPIVerticle extends RestAPIVerticle {

    private AuthenticationService authService;

    private static final String SERVICE_NAME = "auth-rest-api";

    private static final String API_LOGIN = "/login";
    private static final String API_TOKEN = "/auth/token";
    private static final String API_ROLE = "/auth/role";
    private static final String API_SIGNUP = "/auth/signup";

    public AuthenticationAPIVerticle(AuthenticationService auth) { this.authService = auth;}

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAPIVerticle.class);

    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // api route handler
        router.post(API_LOGIN).handler(this::createToken);
        router.post(API_TOKEN).handler(this::verifyToken);
        router.post(API_ROLE).handler(this::getRole);
        router.post(API_SIGNUP).handler(this::signUp);

        String host = config().getString("auth.http.address", "authentication-microservice");
        int port = config().getInteger("auth.http.port", 8086);
        String apiName = config().getString("api.name", "auth-api");

        // create HTTP server and publish REST service
        createHttpServer(router, host, port)
                .compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port, apiName))
                .onComplete(promise);
    }

    private void getRole (RoutingContext context) {
        String token = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        logger.info("ROLE TOKEN: " + token);
        authService.getRole(token, resultHandlerNonEmpty(context));
    }

    private void createToken (RoutingContext context) {
        JsonObject user = context.getBodyAsJson();
        authService.createToken(user,resultHandlerNonEmpty(context));
    }

    private void verifyToken (RoutingContext context) {
        String token = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        authService.verifyToken(token, resultVoidHandler(context, 200));
    }

    private void signUp(RoutingContext context) {
        JsonObject user = context.getBodyAsJson();
        authService.signUp(user, resultHandlerNonEmpty(context));
    }
}
