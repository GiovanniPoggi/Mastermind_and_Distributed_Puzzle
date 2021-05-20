package mongo.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import mongo.MongoDBService;
import mongo.MongoDBUsers;

import java.util.List;

/**
 * This Class implements methods of MongoDBService.
 */
public class MongoDBServiceImpl implements MongoDBService {

    /**
     * Fields that represents che collection inside MongoDB to use to manage messages and users.
     */
    private static final String COLLECTION_USER = "mongoDBUsers";

    /**
     * Field that represent the instance of MongoClient.
     */
    private final MongoClient client;

    /**
     * Constructor to set field vertx and config.
     *
     * @param vertx
     * @param config
     */
    public MongoDBServiceImpl(Vertx vertx, JsonObject config) {
        this.client = MongoClient.create(vertx, config);
    }

    /**
     * Proxy method to save a new Database for the Puzzle's User to the persistence layer. This is a so called `upsert` operation.
     * This is used to update Database info, or just apply for a new Database.
     *
     * @param mongoDocument - Json Object of the Document to create.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    @Override
    public void saveUserToStoreProxy(JsonObject mongoDocument, Handler<AsyncResult<Void>> resultHandler) {
        client.save(COLLECTION_USER, new JsonObject().put("id", mongoDocument.getValue("id"))
                        .put("name", mongoDocument.getValue("name"))
                        .put("username", mongoDocument.getValue("username"))
                        .put("email", mongoDocument.getValue("email"))
                        .put("password", mongoDocument.getValue("password"))
                        .put("role", mongoDocument.getValue("role")),
                ar -> {
                    if (ar.succeeded()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(ar.cause()));
                    }
                }
        );
    }

    /**
     * API method to save a new Database for the Puzzle's User to the persistence layer. This is a so called `upsert` operation.
     * This is used to update Database info, or just apply for a new Database.
     *
     * @param mongoDocument - Json Object of the Document to create.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    @Override
    public void saveUserToStoreAPI(MongoDBUsers mongoDocument, Handler<AsyncResult<Void>> resultHandler) {
        client.save(COLLECTION_USER, new JsonObject().put("id", mongoDocument.getId())
                        .put("name", mongoDocument.getName())
                        .put("username", mongoDocument.getUsername())
                        .put("email", mongoDocument.getEmail())
                        .put("password", mongoDocument.getPassword())
                        .put("role", mongoDocument.getRole()),
                ar -> {
                    if (ar.succeeded()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(ar.cause()));
                    }
                }
        );
    }

    /**
     * Method to Get a User with a certain Username.
     *
     * @param username - Username of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    @Override
    public void getUser(String username, Handler<AsyncResult<MongoDBUsers>> resultHandler) {
        JsonObject query = new JsonObject().put("username", username);
        client.findOne(COLLECTION_USER, query, null, ar -> {
            if (ar.succeeded()) {
                if (ar.result() == null) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    MongoDBUsers mongoDBUsers = new MongoDBUsers(ar.result().put("id", ar.result().getString("id"))
                            .put("name", ar.result().getString("name"))
                            .put("username", ar.result().getString("username"))
                            .put("email", ar.result().getString("email"))
                            .put("password", ar.result().getString("password"))
                            .put("role", ar.result().getString("role")));
                    resultHandler.handle(Future.succeededFuture(mongoDBUsers));
                }
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    /**
     * Method to Get all Messages or Users of the Global Puzzle stored into the Database.
     *
     * @param id - Id of the Database where are stored all messages/users.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    @Override
    public void getAllFromDatabase(String id, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        JsonObject query = new JsonObject().put("id", id);
        client.find(COLLECTION_USER, query, x -> {
            if (x.succeeded()) {
                if (x.result() == null) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    List<JsonObject> allMsg = x.result();
                    resultHandler.handle(Future.succeededFuture(allMsg));
                }
            } else {
                resultHandler.handle(Future.failedFuture(x.cause()));
            }
        });
    }

    /**
     * Method to Delete a Database with a certain ID.
     *
     * @param id - Id of the Database to delete.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     */
    @Override
    public void deleteCollection(String id, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject query = new JsonObject().put("id", id);
        client.removeDocument(COLLECTION_USER, query, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
