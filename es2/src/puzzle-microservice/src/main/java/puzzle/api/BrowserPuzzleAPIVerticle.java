package puzzle.api;

import common.RestAPIVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import puzzle.PuzzleService;

/**
 * Class that manages the API of the Puzzle Microservice.
 */
public class BrowserPuzzleAPIVerticle extends RestAPIVerticle {

    /**
     * Field that represent the Name of the Service of Puzzle.
     */
    private static final String SERVICE_NAME = "puzzle-api-rest";

    /**
     * Field that create PuzzleService instance.
     */
    private final PuzzleService puzzleService;

    /**
     * Fields that represents the API of the Game.
     */
    private static final String API_PLAY = "/play";
    private static final String API_JOIN = "/join";
    private static final String API_SWAP = "/swap";
    private static final String API_FAIL = "/fail";

    private static final Logger logger = LoggerFactory.getLogger(BrowserPuzzleAPIVerticle.class);

    /**
     * Constructor to save Puzzle Service.
     * @param puzzleService of the game.
     */
    public BrowserPuzzleAPIVerticle(PuzzleService puzzleService) {
        this.puzzleService = puzzleService;
    }

    /**
     *
     * @param promise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // api route handler
        router.post(API_PLAY).handler(this::createTable);
        router.post(API_JOIN).handler(this::joinTable);
        router.post(API_SWAP).handler(this::swapTiles);
        router.post(API_FAIL).handler(this::fail);

        String host = config().getString("puzzle-microservice.http.address", "puzzle-microservice");
        int port = config().getInteger("puzzle-microservice.http.port", 8081);
        String apiName = config().getString("api.name", "puzzle-api");

        // create HTTP server and publish REST service
        createHttpServer(router, host, port)
                .compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port, apiName))
                .onComplete(promise);
    }

    private void fail(RoutingContext routingContext) {
        this.puzzleService.fail(resultHandlerNonEmpty(routingContext));
    }

    /**
     * Method that let User to Join in a Game.
     * @param routingContext
     */
    private void joinTable(RoutingContext routingContext) {
        String username = routingContext.getBodyAsJson().getString("username");
        this.puzzleService.joinTable(username, resultHandlerNonEmpty(routingContext));
    }

    /**
     * Method that manage the Swap of two Pieces of Puzzle.
     * @param routingContext
     */
    private void swapTiles(RoutingContext routingContext) {
        int x = Integer.parseInt(routingContext.getBodyAsJson().getString("position0"));
        int y = Integer.parseInt(routingContext.getBodyAsJson().getString("position1"));

        logger.info("POS1: " + x + "POS 2: " + y);
        this.puzzleService.swap(x, y, resultVoidHandler(routingContext, 200));
    }

    /**
     * Method that create the Table with random pieces of Puzzle.
     * @param routingContext
     */
    private void createTable(RoutingContext routingContext) {
        int cols = Integer.parseInt(routingContext.getBodyAsJson().getString("dimensionCols"));
        String imageURL = routingContext.getBodyAsJson().getString("imgUrl");
        String username = routingContext.getBodyAsJson().getString("username");//"https://upload.wikimedia.org/wikipedia/commons/6/6e/Bletchley_Park.jpg";
        this.puzzleService.createTiles(imageURL, cols, username, resultHandlerNonEmpty(routingContext));
    }
}