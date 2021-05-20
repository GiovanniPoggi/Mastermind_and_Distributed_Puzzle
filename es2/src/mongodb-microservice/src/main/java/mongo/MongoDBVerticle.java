package mongo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import common.BaseMicroserviceVerticle;
import mongo.api.MongoDBAPIVerticle;
import mongo.impl.MongoDBServiceImpl;

import static mongo.MongoDBService.SERVICE_ADDRESS;
import static mongo.MongoDBService.SERVICE_NAME;

/**
 * This Class represent a verticle to store operation (apply or close) processing.
 */
public class MongoDBVerticle extends BaseMicroserviceVerticle {

    /**
     * Field that create MongoDBService instance.
     */
    private MongoDBService crudService;

    /**
     * Method that create the service instance.
     *
     * @param promise
     * @throws Exception - if the calls goes wrong.
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();

        crudService = new MongoDBServiceImpl(vertx, config());
        binder.setAddress(MongoDBService.SERVICE_ADDRESS);
        binder.register(MongoDBService.class, crudService);
        // publish service and deploy REST verticle
        publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, MongoDBService.class)
                .compose(servicePublished -> deployRestVerticle(crudService))
                .onComplete(promise);
    }

    /**
     * Method that deploy the Rest Verticle.
     *
     * @param service - set of sockets that represent connected MongoDB service
     * @return the promise of deploy, asynchronous result.
     */
    private Future<Void> deployRestVerticle(MongoDBService service) {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(new MongoDBAPIVerticle(service),
                new DeploymentOptions().setConfig(config()),
                promise);
        return promise.future().map(r -> null);
    }
}
