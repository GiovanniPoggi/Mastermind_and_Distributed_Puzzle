package gateway;

import common.RestAPIVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.List;
import java.util.Optional;

/**
 * This Class provides basic Gateway service.
 */
public class APIGatewayVerticle extends RestAPIVerticle {

    /**
     * Field of the default port.
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * Field to log the code.
     */
    private static final Logger logger = LoggerFactory.getLogger(gateway.APIGatewayVerticle.class);

    /**
     *
     * @param promise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("api.gateway.http.address", "localhost");
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

        Router router = Router.router(vertx);

        // cookie and session handler
        //enableLocalSession(router);

        // body handler
        router.route().handler(BodyHandler.create());

        // version handler
        router.get("/api/v").handler(this::apiVersion);

        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);

        // static content
        router.route().handler(StaticHandler.create().setCachingEnabled(false));

        // enable HTTPS
        /*
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("server.jks").setPassword("123456"));
         */

        // create http server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, ar -> {
                    if (ar.succeeded()) {
                        publishApiGateway(host, port);
                        promise.complete();
                        logger.info("API Gateway is running on port " + port);
                    } else {
                        promise.fail(ar.cause());
                    }
                });
    }

    /**
     * This method parses the request url in order to find to which microservice redirects the request.
     * After that it checks if the microservice is available and forward to him the request.
     *
     * @param context - Routing context.
     */
    private void dispatchRequests(RoutingContext context) {
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(promise -> {
            getAllEndpoints().onComplete(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    // get relative path and retrieve prefix to dispatch client
                    String path = context.request().uri();

                    if (path.length() <= initialOffset) {
                        notFound(context);
                        promise.complete();
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                            .split("/"))[0];
                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());
                    // get one relevant HTTP client, may not exist
                    Optional<Record> client = recordList.stream()
                            .filter(record -> record.getMetadata().getString("api.name") != null)
                            .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                            .findAny(); // simple load balance
                    if (client.isPresent()) {
                        doDispatch(context, newPath, discovery.getReference(client.get()).get(), promise);
                    } else {
                        notFound(context);
                        promise.complete();
                    }
                } else {
                    promise.fail(ar.cause());
                }
            });
        }).onComplete(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context - routing context instance.
     * @param path - a relative path.
     * @param client - relevant HTTP client.
     * @param cbFuture
     */
    private void doDispatch(RoutingContext context, String path, HttpClient client, Promise<Object> cbFuture) {
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.headers().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                    });
                });
        // set headers
        context.request().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        if (context.user() != null) {
            toReq.putHeader("user-principal", context.user().principal().encode());
        }
        // send request
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    /**
     * Method that set the API version.
     *
     * @param context - RoutingContext.
     */
    private void apiVersion(RoutingContext context) {
        context.response()
                .end(new JsonObject().put("version", "v1").encodePrettily());
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return asynchronous result.
     */
    private Future<List<Record>> getAllEndpoints() {
        Promise<List<Record>> promise = Promise.promise();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                promise);
        return promise.future();
    }

    /**
     * Method to Log publish Gateway from String.
     *
     * @param info - String that represents the info to display.
     */
    private void publishGatewayLog(String info) {
        JsonObject message = new JsonObject()
                .put("info", info)
                .put("time", System.currentTimeMillis());
        publishLogEvent("gateway", message);
    }

    /**
     * Method to Log publish Gateway from JsonObject.
     *
     * @param msg - String that represents the info to display.
     */
    private void publishGatewayLog(JsonObject msg) {
        JsonObject message = msg.copy()
                .put("time", System.currentTimeMillis());
        publishLogEvent("gateway", message);
    }
}