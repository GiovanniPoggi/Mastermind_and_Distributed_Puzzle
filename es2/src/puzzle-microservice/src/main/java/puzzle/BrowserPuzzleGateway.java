package puzzle;

import common.RestAPIVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

/**
 * Class that Manages the Gateway of the Puzzle's Microservice of the Game.
 */
public class BrowserPuzzleGateway extends RestAPIVerticle {

    private static final String EVENTBUS = "/eventbus";
    private static final Logger logger = LoggerFactory.getLogger(BrowserPuzzleGateway.class);

    /**
     *
     * @param promise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.mountSubRouter(EVENTBUS, puzzleEventBusHandler());

        String host = config().getString("puzzle-microservice.http.address", "puzzle-microservice");
        int port = config().getInteger("puzzle-microservice.http.port", 9001);

        // create HTTP server
        createHttpServer(router, host, port)
                .onComplete(promise);
    }

    /**
     *
     * @return SockJSHandler that manages web socket connections from the browser to the server.
     */
    private Router puzzleEventBusHandler() {
        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);

        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("global_puzzle\\.[0-99]+"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex("global_puzzle\\.[0-99]+"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex("puzzle_users\\.[0-99]+"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("puzzle_users\\.[0-99]+"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("end\\.[0-99]+"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex("end\\.[0-99]+"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("newOnlineUser\\.[0-99]+"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex("newOnlineUser\\.[0-99]+"));

        return SockJSHandler.create(vertx, options).bridge(bridgeOptions, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                logger.info("A web socket was created !");
            } else if (event.type() == BridgeEventType.SOCKET_CLOSED){
                logger.info("A web socket was closed!");
            } else if (event.type() == BridgeEventType.UNREGISTER || event.type() == BridgeEventType.REGISTER){
                logger.info("DEBUG" + event.getRawMessage().encode());
            }
            event.complete(true);
        });
    }
}