package common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * This class provides Rest functions to all API verticles
 */
public abstract class RestAPIVerticle extends BaseMicroserviceVerticle {

    /**
     * Create http server for the REST service.
     *
     * @param router router instance
     * @param host   http host
     * @param port   http port
     * @return async result of the procedure
     */
    protected Future<Void> createHttpServer(Router router, String host, int port) {
        Promise<HttpServer> httpServerPromise = Promise.promise();
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, host, httpServerPromise);
        return httpServerPromise.future().map(r -> null);
    }

    /**
     * Enable CORS support.
     *
     * @param router router instance
     */
    protected void enableCorsSupport(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.PUT);
        allowMethods.add(HttpMethod.OPTIONS);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));
    }

    // helper result handler within a request context

    /**
     * This method generates handler for async methods in REST APIs.
     */
    protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Handler<T> handler) {
        return res -> {
            if (res.succeeded()) {
                handler.handle(res.result());
            } else {
                internalError(context, res.cause());
                res.cause().printStackTrace();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * Use the result directly and invoke `toString` as the response. The content type is JSON.
     */
    protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(res == null ? "{}" : res.toString());
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * Use the result directly and use given {@code converter} to convert result to string
     * as the response. The content type is JSON.
     *
     * @param context   routing context instance
     * @param converter a converter that converts result to a string
     * @param <T>       result type
     * @return generated handler
     */
    protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Function<T, String> converter) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res == null) {
                    serviceUnavailable(context, "invalid_result");
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(converter.apply(res));
                }
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result requires non-empty. If empty, return <em>404 Not Found</em> status.
     * The content type is JSON.
     *
     * @param context routing context instance
     * @param <T>     result type
     * @return generated handler
     */
    protected <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res == null) {
                    notFound(context);
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                }
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The content type is originally raw text.
     *
     * @param context routing context instance
     * @param <T>     result type
     * @return generated handler
     */
    protected <T> Handler<AsyncResult<T>> rawResultHandler(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                context.response()
                        .end(res == null ? "" : res.toString());
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result) {
        return resultVoidHandler(context, result, 200);
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result is not needed. Only the state of the async result is required.
     *
     * @param context routing context instance
     * @param result  result content
     * @param status  status code
     * @return generated handler
     */
    protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result, int status) {
        return ar -> {
            if (ar.succeeded()) {
                context.response()
                        .setStatusCode(status == 0 ? 200 : status)
                        .putHeader("content-type", "application/json")
                        .end(result.encodePrettily());
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, int status) {
        return ar -> {
            if (ar.succeeded()) {
                context.response()
                        .setStatusCode(status == 0 ? 200 : status)
                        .putHeader("content-type", "application/json")
                        .end();
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST DELETE APIs.
     * Return format in JSON (successful status = 204):
     * <code>
     * {"message": "delete_success"}
     * </code>
     *
     * @param context routing context instance
     * @return generated handler
     */
    protected Handler<AsyncResult<Void>> deleteResultHandler(RoutingContext context) {
        return res -> {
            if (res.succeeded()) {
                context.response().setStatusCode(204)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("message", "delete_success").encodePrettily());
            } else {
                internalError(context, res.cause());
                res.cause().printStackTrace();
            }
        };
    }

    /**
     *
     * @param context routing context
     * @param ex Exception
     */
    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    /**
     *
     * @param context Routing context
     */
    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }

    /**
     *
     * @param context Routing context
     * @param ex Exception
     */
    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_implemented").encodePrettily());
    }

    /**
     *
     * @param ex Exception
     * @param context Routing context
     */
    protected void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "bad_gateway")
                        //.put("message", ex.getMessage())
                        .encodePrettily());
    }

    /**
     *
     * @param context Routing context
     */
    protected void serviceUnavailable(RoutingContext context) {
        context.fail(503);
    }

    /**
     *
     * @param context routing context
     * @param ex Exception
     */
    protected void serviceUnavailable(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    /**
     *
     * @param context Routing context
     * @param cause Error cause
     */
    protected void serviceUnavailable(RoutingContext context, String cause) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }

}
