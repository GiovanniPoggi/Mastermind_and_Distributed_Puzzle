package mysql;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Interface that manage all User Accounts and Messages into the Database.
 */
@VertxGen
@ProxyGen
public interface AccountService {

    /**
     * The name of the event bus service.
     */
    String SERVICE_NAME = "user-account-eb-service";

    /**
     * The address on which the service was publish.
     */
    String SERVICE_ADDRESS = "service.user.account";

    /**
     * Method that initialize the Database to Store all User of the Global Puzzle.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService createUserDatabase(Handler<AsyncResult<Void>> resultHandler);

    /**
     * Proxy method that add a User to the Database.
     *
     * @param account - Json Object that represent all Information of the User that we want to add.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService addUserProxy(JsonArray account, Handler<AsyncResult<Void>> resultHandler);

    /**
     * API method that add a User to the Database.
     *
     * @param account - Account Object that represent all Information of the User that we want to add.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService addUserAPI(Account account, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Method that get the ID of the last User added to the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService getLastUserID(Handler<AsyncResult<List<Account>>> resultHandler);

    /**
     * Method that get the User into the Database with certain ID.
     *
     * @param id - id of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService getUserById(String id, Handler<AsyncResult<Account>> resultHandler);

    /**
     * Method that get the User into the Database with certain USERNAME.
     *
     * @param username - username of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService getByUsername(String username, Handler<AsyncResult<Account>> resultHandler);

    /**
     * Method that get all the Users into the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService getAllUsers(Handler<AsyncResult<List<Account>>> resultHandler);

    /**
     * Method that get a certain User and Update it into the Database.
     *
     * @param account - Json Object that have all the new information of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService updateUserProxy(JsonObject account, Handler<AsyncResult<Account>> resultHandler);

    /**
     * Method that get a certain User and Update it into the Database.
     *
     * @param account - Account Object that have all the new information of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService updateUserAPI(Account account, Handler<AsyncResult<Account>> resultHandler);

    /**
     * Method that delete a User from the Database with a certain ID.
     *
     * @param username - username of the User that we want to delete.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService deleteUser(String username, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Method that delete all User from the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService deleteAllUsers(Handler<AsyncResult<Void>> resultHandler);

    /**
     * Method that delete the Database of the Users.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Fluent
    AccountService deleteDatabaseUsers(Handler<AsyncResult<Void>> resultHandler);
}