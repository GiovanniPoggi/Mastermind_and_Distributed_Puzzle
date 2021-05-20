package puzzle;

import common.BaseMicroserviceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import puzzle.api.BrowserPuzzleAPIVerticle;
import puzzle.impl.PuzzleServiceImpl;

/**
 * Class that manage the Verticle of the Puzzle Microservice of the Game.
 */
public class BrowserPuzzleVerticle extends BaseMicroserviceVerticle {

    private PuzzleService puzzleService;

    /**
     * Method that create the service instance.
     *
     * @param promise
     * @throws Exception - if the calls goes wrong.
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        // create the service instance
        puzzleService = new PuzzleServiceImpl(vertx) {
        };
        binder.setAddress(PuzzleService.SERVICE_ADDRESS);
        binder.register(PuzzleService.class, puzzleService);
        // publish the service and REST endpoint in the discovery infrastructure
        publishEventBusService(PuzzleService.SERVICE_NAME, PuzzleService.SERVICE_ADDRESS, PuzzleService.class)
                .compose(servicePublished -> deployRestVerticle())
                .compose(verticleDeployed -> deployBrowserBusGatewayVerticle())
                .onComplete(promise);
    }

    /**
     * Method that deploy the Rest Verticle.
     * @return the promise of deploy, asynchronous result.
     */
    private Future<Void> deployRestVerticle() {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(new BrowserPuzzleAPIVerticle(puzzleService),
                new DeploymentOptions().setConfig(config()),
                promise);
        return promise.future().map(r -> null);
    }

    /**
     * Deploys the BrowserBus Gateway Verticle.
     * @return the promise of deploy, asynchronous result.
     */
    private Future<Void> deployBrowserBusGatewayVerticle() {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(new BrowserPuzzleGateway(),
                promise);
        return promise.future().map(r -> null);
    }
}