package common;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class provide basic functions to all microservice verticle
 */
public abstract class BaseMicroserviceVerticle extends AbstractVerticle {

    private static final String LOG_EVENT_ADDRESS = "events.log";

    private static final Logger logger = LoggerFactory.getLogger(BaseMicroserviceVerticle.class);

    protected ServiceBinder binder;
    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

    @Override
    public void start() throws Exception {

        // init service discovery instance
        binder = new ServiceBinder(vertx);
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions()
                .setBackendConfiguration(
                        new JsonObject()
                                .put("host", "redis-service")
                                .put("port", "6379")
                                .put("key", "records")
                ));

        // init circuit breaker instance
        JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                config().getJsonObject("circuit-breaker") : new JsonObject();
        circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                        .setTimeout(cbOptions.getLong("timeout", 10000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 10000L))
        );
    }

    /**
     * This method publishes a new Httpd endpoint service
     *
     * @param name name of the service
     * @param host host of the service
     * @param port port of the service
     * @param apiName api name of the service
     * @return async result
     */
    protected Future<Void> publishHttpEndpoint(String name, String host, int port, String apiName) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", apiName)
        );
        return publish(record);
    }

    /**
     * This method publishes the API Gateway
     *
     * @param host host of the Gateway
     * @param port port of the Gateway
     * @return async result
     */
    protected Future<Void> publishApiGateway(String host, int port) {
        Record record = HttpEndpoint.createRecord("api-gateway", host, port, "/", null)
                .setType("api-gateway");
        return publish(record);
    }

    /**
     * This method publishes an EventBus service
     *
     * @param name name of the service
     * @param address adress of the service
     * @param serviceClass Class of the service
     * @return async result
     */
    protected Future<Void> publishEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        return publish(record);
    }

    /**
     * Publish a service with record.
     *
     * @param record service record
     * @return async result
     */
    private Future<Void> publish(Record record) {
        if (discovery == null) {
            try {
                start();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create discovery service");
            }
        }

        Promise<Void> promise = Promise.promise();
        // publish the service
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                logger.info("Service <" + ar.result().getName() + "> published");;
                logger.info(ar.result().toJson());
                registeredRecords.add(ar.result());
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    /**
     * A helper method that simply publish logs on the event bus.
     *
     * @param type log type
     * @param data log message data
     */
    protected void publishLogEvent(String type, JsonObject data) {
        JsonObject msg = new JsonObject().put("type", type)
                .put("message", data);
        vertx.eventBus().publish(LOG_EVENT_ADDRESS, msg);
    }

    protected void publishLogEvent(String type, JsonObject data, boolean succeeded) {
        JsonObject msg = new JsonObject().put("type", type)
                .put("status", succeeded)
                .put("message", data);
        vertx.eventBus().publish(LOG_EVENT_ADDRESS, msg);
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception {
        //the publisher is responsible for removing the service
        List<Future> futures = new ArrayList<>();
        registeredRecords.forEach(record -> {
            Promise<Void> cleanupPromise = Promise.promise();
            futures.add(cleanupPromise.future());
            discovery.unpublish(record.getRegistration(), cleanupPromise);
        });

        if (futures.isEmpty()) {
            discovery.close();
            promise.complete();
        } else {
            CompositeFuture.all(futures)
                    .onComplete(ar -> {
                        discovery.close();
                        if (ar.failed()) {
                            promise.fail(ar.cause());
                        } else {
                            promise.complete();
                        }
                    });
        }
    }
}
