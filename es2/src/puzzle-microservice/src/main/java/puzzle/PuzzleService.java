package puzzle;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface PuzzleService {
    /**
     * The name of the event bus service.
     */
    String SERVICE_NAME = "puzzle-service";

    /**
     * The address on which the service was publish.
     */
    String SERVICE_ADDRESS = "service.puzzle";

    /**
     * Method that create a List with all pieces of puzzle.
     * @param imageURL - Image Url of Image.
     * @param col - Number of Column and Rows of Puzzle.
     * @param username - Username of the user that create a Game.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     *                        The async result indicates whether the operation was successful or not.
     */
    void createTiles(String imageURL, int col, String username, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Method that Swap two pieces of the Puzzle.
     * @param t1 - First Piece of Puzzle.
     * @param t2 - Second Piece of Puzzle to Swap.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     *                        The async result indicates whether the operation was successful or not.
     */
    void swap(int t1, int t2, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Method that let User to Join into another Game Puzzle.
     * @param username - Username of the user that want to Join in a Game.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     *                        The async result indicates whether the operation was successful or not.
     */
    void joinTable(String username, Handler<AsyncResult<JsonObject>> resultHandler);

    void fail(Handler<AsyncResult<Void>> resultHandler);
}
