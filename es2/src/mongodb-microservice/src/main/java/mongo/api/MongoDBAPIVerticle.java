package mongo.api;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import common.RestAPIVerticle;
import mongo.MongoDBService;
import mongo.MongoDBUsers;

/**
 * This Class represents the verticle that provides the REST API for the Database service.
 */
public class MongoDBAPIVerticle extends RestAPIVerticle {

    /**
     * Field that represent the Name of the Service of MongoDB.
     */
    private static final String SERVICE_NAME = "mongodb-rest-api";

    /**
     * Field that create MongoDBService instance.
     */
    private final MongoDBService service;

    /**
     * Fields that represents the String Path for the Routers.
     */
    private static final String PROXY_SAVE_USER = "/saveUserProxy";
    private static final String API_SAVE_USER = "/saveUserAPI";
    private static final String API_GET_USER_FROM_USERNAME = "/user/username";
    private static final String API_GET_ALL_FROM_DATABASE = "/:id";
    private static final String API_GET_ALL_MSG_FROM_DATABASE = "/message/:id";
    private static final String API_DELETE = "/:id";

    /**
     * Constructor to set the MongoDBService field.
     *
     * @param service - field to set.
     */
    public MongoDBAPIVerticle(MongoDBService service) {
        this.service = service;
    }

    /**
     *
     * @param promise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();

        Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post(PROXY_SAVE_USER).handler(this::apiSaveUserProxy);
        router.post(API_SAVE_USER).handler(this::apiSaveUserAPI);
        router.post(API_GET_USER_FROM_USERNAME).handler(this::apiGetUser);
        router.post(API_GET_ALL_FROM_DATABASE).handler(this::apiGetAllFromDatabase);
        router.delete(API_DELETE).handler(this::apiDeleteCollection);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("mongodb-microservice.http.address", "mongodb-microservice");
        int port = config().getInteger("mongodb-microservice.http.port", 8083);
        String apiName = config().getString("api.name", "mongo-api");

        createHttpServer(router, host, port)
                .compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port, apiName))
                .onComplete(promise);
    }

    /**
     * Method to save into the Database a new User from Json Object.
     *
     * @param context - RoutingContext.
     */
    private void apiSaveUserProxy(RoutingContext context) {
        JsonObject mongoDBUsers = new JsonObject(context.getBodyAsString());
        if (mongoDBUsers.getValue("id") == null) {
            badRequest(context, new IllegalStateException("ERROR: ID can't be null"));
        } else {
            JsonObject result = new JsonObject().put("user", "store_saved")
                    .put("id", mongoDBUsers.getValue("id"));
            service.saveUserToStoreProxy(mongoDBUsers, resultVoidHandler(context, result));
        }
    }

    /**
     * Method to save into the Database a new User from MongoDBUser Object.
     *
     * @param context - RoutingContext.
     */
    private void apiSaveUserAPI(RoutingContext context) {
        MongoDBUsers mongoDBUser = new MongoDBUsers(new JsonObject(context.getBodyAsString()));
        if (mongoDBUser.getId() == null) {
            badRequest(context, new IllegalStateException("ERROR: ID can't be null"));
        } else {
            JsonObject result = new JsonObject().put("user", "store_saved")
                    .put("id", mongoDBUser.getId())
                    .put("name", mongoDBUser.getName())
                    .put("username", mongoDBUser.getUsername())
                    .put("email", mongoDBUser.getEmail())
                    .put("password", mongoDBUser.getPassword())
                    .put("role", mongoDBUser.getRole());
            service.saveUserToStoreAPI(mongoDBUser, resultVoidHandler(context, result));
        }
    }

    /**
     * Method to Get from the User Database a User with a certain Username.
     *
     * @param context - RoutingContext.
     */
    private void apiGetUser(RoutingContext context) {
        String username = context.getBodyAsJson().getString("username");
        service.getUser(username, resultHandlerNonEmpty(context));
    }

    /**
     * Method to Get all data inside a Database with certain ID.
     *
     * @param context - RoutingContext.
     */
    private void apiGetAllFromDatabase(RoutingContext context) {
        String id = context.request().getParam("id");
        service.getAllFromDatabase(id, resultHandlerNonEmpty(context));
    }

    /**
     * Method to Delete a Collection from the Database with a certain ID.
     *
     * @param context - RoutingContext.
     */
    private void apiDeleteCollection(RoutingContext context) {
        String id = context.request().getParam("id");
        service.deleteCollection(id, deleteResultHandler(context));
    }
}
