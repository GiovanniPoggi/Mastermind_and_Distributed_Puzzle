package mongo;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Interface that manage all Message and User into the Database.
 */
@VertxGen
@ProxyGen
public interface MongoDBService {

    /**
     * The name of the event bus service.
     */
    String SERVICE_NAME = "store-eb-service";

    /**
     * The address on which the service was publish.
     */
    String SERVICE_ADDRESS = "service.store";

    /**
     * Proxy method to save a new Database for the Puzzle's User to the persistence layer. This is a so called `upsert` operation.
     * This is used to update Database info, or just apply for a new Database.
     *
     * @param mongoDocument - Json Object of the Document to create.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    void saveUserToStoreProxy(JsonObject mongoDocument, Handler<AsyncResult<Void>> resultHandler);

    /**
     * API method to save a new Database for the Puzzle's User to the persistence layer. This is a so called `upsert` operation.
     * This is used to update Database info, or just apply for a new Database.
     *
     * @param mongoDocument - Json Object of the Document to create.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    void saveUserToStoreAPI(MongoDBUsers mongoDocument, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Method to Get a User with a certain Username.
     *
     * @param username - Username of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    void getUser(String username, Handler<AsyncResult<MongoDBUsers>> resultHandler);

    /**
     * Method to Get all Messages or Users of the Global Puzzle stored into the Database.
     *
     * @param id - Id of the Database where are stored all messages/users.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    void getAllFromDatabase(String id, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    /**
     * Method to Delete a Database with a certain ID.
     *
     * @param id - Id of the Database to delete.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    void deleteCollection(String id, Handler<AsyncResult<Void>> resultHandler);

}
