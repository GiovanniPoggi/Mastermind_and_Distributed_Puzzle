package mysql.api;

import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import common.RestAPIVerticle;
import mysql.Account;
import mysql.AccountService;

/**
 * This Class represents the verticle that exposes an HTTP endpoint to process User data via REST API.
 */
public class MySQLAPIVerticle extends RestAPIVerticle {

    /**
     * Field that represent the Name of the Service of MySQL.
     */
    private static final String SERVICE_NAME = "user-account-rest-api";

    /**
     * Field that create AccountService instance.
     */
    private final AccountService accountService;

    /**
     * Fields that represents the String Path for the Routers.
     */
    private static final String API_ADD_USER = "/userAPI";
    private static final String PROXY_ADD_USER = "/userProxy";
    private static final String API_GET_LAST_USER_ID = "/getLastUserId";
    private static final String API_GET_USER_BY_ID = "/getUserById/:id";
    private static final String API_GET_USER_BY_USERNAME = "/user/username";
    private static final String API_GET_ALL_USERS = "/user";
    private static final String API_UPDATE_USER = "/updateUserAPI";
    private static final String PROXY_UPDATE_USER = "/updateUserProxy";
    private static final String API_DELETE_USER = "/user/username";

    /**
     * Constructor to set the AccountService field.
     *
     * @param accountService - field to set.
     */
    public MySQLAPIVerticle(AccountService accountService) {
        this.accountService = accountService;
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
        router.post(API_ADD_USER).handler(this::apiAddUserAPI);
        router.post(PROXY_ADD_USER).handler(this::apiAddUserProxy);
        router.get(API_GET_LAST_USER_ID).handler(this::apiGetLastUserID);
        router.get(API_GET_USER_BY_ID).handler(this::apiGetUserById);
        router.post(API_GET_USER_BY_USERNAME).handler(this::apiGetByUsername);
        router.get(API_GET_ALL_USERS).handler(this::apiGetAllUsers);
        router.put(API_UPDATE_USER).handler(this::apiUpdateUserAPI);
        router.get(PROXY_UPDATE_USER).handler(this::apiUpdateUserProxy);
        router.delete(API_DELETE_USER).handler(this::apiDeleteUser);

        String host = config().getString("mysql-microservice.http.address", "mysql-microservice");
        int port = config().getInteger("mysql-microservice.http.port", 8084);
        String apiName = config().getString("api.name", "mysql-api");

        // create HTTP server and publish REST service
        createHttpServer(router, host, port)
                .compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port, apiName))
                .onComplete(promise);
    }

    /**
     * Method to add a new User to the Database from Json Object.
     *
     * @param context - RoutingContext.
     */
    private void apiAddUserProxy(RoutingContext context) {
        JsonArray account = context.getBodyAsJsonArray();
        accountService.addUserProxy(account, resultVoidHandler(context, 201));
    }

    /**
     * Method to add a new User to Database from Account Object.
     *
     * @param context - RoutingContext.
     */
    private void apiAddUserAPI(RoutingContext context) {
        Account account = new Account(context.getBodyAsJson());
        accountService.addUserAPI(account, resultVoidHandler(context, 201));
    }

    /**
     * Method to get the Last User added to the Database of the Chat.
     *
     * @param context - RoutingContext.
     */
    private void apiGetLastUserID(RoutingContext context) {
        accountService.getLastUserID(resultHandlerNonEmpty(context));
    }

    /**
     * Method to get the Account with a certain User ID added to the Database.
     *
     * @param context - RoutingContext.
     */
    private void apiGetUserById(RoutingContext context) {
        String id = context.request().getParam("id");
        accountService.getUserById(id, resultHandlerNonEmpty(context));
    }

    /**
     * Method to get an Account with a certain Username from the Database.
     *
     * @param context - RoutingContext.
     */
    private void apiGetByUsername(RoutingContext context) {
        String username = context.getBodyAsJson().getString("username");
        accountService.getByUsername(username, resultHandlerNonEmpty(context));
    }

    /**
     * Method to get all Account in the Database of the Chat.
     *
     * @param context - RoutingContext.
     */
    private void apiGetAllUsers(RoutingContext context) {
        accountService.getAllUsers(resultHandler(context, Json::encodePrettily));
    }

    /**
     * Method to update fields of the Global Puzzle's User from Json.
     *
     * @param context - RoutingContext.
     */
    private void apiUpdateUserProxy(RoutingContext context) {
        JsonObject user = context.getBodyAsJson();
        accountService.updateUserProxy(user, resultHandlerNonEmpty(context));
    }

    /**
     * Method to update fields of the Global Puzzle's User from Account Class.
     *
     * @param context - RoutingContext.
     */
    private void apiUpdateUserAPI(RoutingContext context) {
        Account account = new Account(context.getBodyAsJson());
        accountService.updateUserAPI(account, resultHandlerNonEmpty(context));
    }

    /**
     * Method to delete a specific User with a certain ID from the Database.
     *
     * @param context - RoutingContext.
     */
    private void apiDeleteUser(RoutingContext context) {
        String username = context.getBodyAsJson().getString("username");
        accountService.deleteUser(username, deleteResultHandler(context));
    }
}