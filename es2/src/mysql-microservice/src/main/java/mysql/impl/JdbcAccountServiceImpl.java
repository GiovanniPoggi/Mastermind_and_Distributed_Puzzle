package mysql.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import mysql.Account;
import mysql.AccountService;
import mysql.JdbcRepositoryWrapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This Class implements JDBC methods of AccountService.
 */
public class JdbcAccountServiceImpl extends JdbcRepositoryWrapper implements AccountService {

    /**
     * Fields that represent the Strings of the Query of Users Database.
     */
    private static final String CREATE_USER_DATABASE = "CREATE TABLE IF NOT EXISTS `user_account` (\n" +
            "  `id` varchar(50) NOT NULL,\n" +
            "  `name` varchar(50) NOT NULL,\n" +
            "  `username` varchar(50) NOT NULL,\n" +
            "  `email` varchar(50) NOT NULL,\n" +
            "  `password` varchar(50) NOT NULL,\n" +
            "  `role` varchar(50) NOT NULL,\n" +
            "  PRIMARY KEY (`username`))";
    private static final String ADD_NEW_USER = "INSERT INTO `user_account` (id, name, username, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String GET_LAST_USER_ID = "SELECT `id` FROM `user_account` WHERE id = (SELECT MAX(`id`) FROM `user_account`)";
    private static final String GET_USER_BY_ID = "SELECT * FROM `user_account` WHERE id = ?";
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM `user_account` WHERE username = ?";
    private static final String GET_ALL_USERS = "SELECT * FROM `user_account`";
    private static final String UPDATE_USER = "UPDATE `user_account`\n" +
            "SET `id` = ?,\n" +
            "`name` = ?,\n" +
            "`email` = ?,\n" +
            "`password` = ?,\n" +
            "`role` = ? \n" +
            "WHERE `username` = ?";
    private static final String DELETE_USER = "DELETE FROM `user_account` WHERE username = ?";
    private static final String DELETE_ALL_USERS = "DELETE FROM `user_account`";
    private static final String DELETE_TABLE_USER = "DROP TABLE `user_account`";

    /**
     * Constructor to set field vertx and config.
     *
     * @param vertx
     * @param config
     */
    public JdbcAccountServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    /**
     * Method that create a UUID for the id of Query's SQL.
     *
     * @return the uuid
     */
    private String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Method that initialize the Database to Store all User of the Global Puzzle.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService createUserDatabase(Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.execute(CREATE_USER_DATABASE, r -> {
                resultHandler.handle(r);
                connection.close();
            });
        }));
        return this;
    }

    /**
     * Proxy method that add a User to the Database.
     *
     * @param account - Json Object that represent all Information of the User that we want to add.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService addUserProxy(JsonArray account, Handler<AsyncResult<Void>> resultHandler) {
        this.executeNoResult(account, ADD_NEW_USER, resultHandler);
        return this;
    }

    /**
     * API method that add a User to the Database.
     *
     * @param account - Account Object that represent all Information of the User that we want to add.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService addUserAPI(Account account, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray params = new JsonArray().add(createUUID())
                .add(account.getName())
                .add(account.getUsername())
                .add(account.getEmail())
                .add(account.getPassword())
                .add(account.getRole());
        this.executeNoResult(params, ADD_NEW_USER, resultHandler);
        return this;
    }

    /**
     * Method that get the ID of the last User added to the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService getLastUserID(Handler<AsyncResult<List<Account>>> resultHandler) {
        this.getAll(GET_LAST_USER_ID)
                .map(rawList -> rawList.stream()
                        .map(Account::new)
                        .collect(Collectors.toList())
                )
                .onComplete(resultHandler);
        return this;
    }

    /**
     * Method that get the User into the Database with certain ID.
     *
     * @param id - id of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService getUserById(String id, Handler<AsyncResult<Account>> resultHandler) {
        this.getOne(id, GET_USER_BY_ID)
                .map(option -> option.map(Account::new).orElse(null))
                .onComplete(resultHandler);
        return this;
    }

    /**
     * Method that get the User into the Database with certain USERNAME.
     *
     * @param username - username of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService getByUsername(String username, Handler<AsyncResult<Account>> resultHandler) {
        this.getOne(username, GET_USER_BY_USERNAME)
                .map(option -> option.map(Account::new).orElse(null))
                .onComplete(resultHandler);
        return this;
    }

    /**
     * Method that get all the Users into the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService getAllUsers(Handler<AsyncResult<List<Account>>> resultHandler) {
        this.getAll(GET_ALL_USERS)
                .map(rawList -> rawList.stream()
                        .map(Account::new)
                        .collect(Collectors.toList())
                )
                .onComplete(resultHandler);
        return this;
    }

    /**
     * Method that get a certain User and Update it into the Database.
     *
     * @param account - Json Object that have all the new information of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService updateUserProxy(JsonObject account, Handler<AsyncResult<Account>> resultHandler) {
        JsonArray params = new JsonArray()
                .add(account.getValue("id"))
                .add(account.getValue("name"))
                .add(account.getValue("email"))
                .add(account.getValue("password"))
                .add(account.getValue("role"))
                .add(account.getValue("username"));
        Account acc = new Account(account.getValue("id").toString(), account.getValue("name").toString(), account.getValue("username").toString(),
                account.getValue("email").toString(), account.getValue("password").toString(), account.getValue("role").toString());
        this.execute(params, UPDATE_USER, acc, resultHandler);
        return this;
    }

    /**
     * Method that get a certain User and Update it into the Database.
     *
     * @param account - Account Object that have all the new information of the User.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService updateUserAPI(Account account, Handler<AsyncResult<Account>> resultHandler) {
        JsonArray params = new JsonArray().add(account.getId())
                .add(account.getName())
                .add(account.getUsername())
                .add(account.getEmail())
                .add(account.getPassword())
                .add(account.getRole());
        this.execute(params, UPDATE_USER, account, resultHandler);
        return this;
    }

    /**
     * Method that delete a User from the Database with a certain ID.
     *
     * @param username - username of the User that we want to delete.
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService deleteUser(String username, Handler<AsyncResult<Void>> resultHandler) {
        this.removeOne(username, DELETE_USER, resultHandler);
        return this;
    }

    /**
     * Method that delete all User from the Database.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService deleteAllUsers(Handler<AsyncResult<Void>> resultHandler) {
        this.removeAll(DELETE_ALL_USERS, resultHandler);
        return this;
    }

    /**
     * Method that delete the Database of the Users.
     *
     * @param resultHandler the result handler will be called as soon as the initialization has been accomplished.
     *                      The async result indicates whether the operation was successful or not.
     * @return AccountService
     */
    @Override
    public AccountService deleteDatabaseUsers(Handler<AsyncResult<Void>> resultHandler) {
        this.removeAll(DELETE_TABLE_USER, resultHandler);
        return this;
    }
}