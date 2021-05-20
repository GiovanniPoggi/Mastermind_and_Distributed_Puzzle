package mysql;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.Optional;

/**
 * This Class manages the JDBC repository services.
 */
public class JdbcRepositoryWrapper {

    /**
     * Field that create JDBCClient.
     */
    protected final JDBCClient client;

    /**
     * Constructor that instantiate the JDBCClient.
     *
     * @param vertx
     * @param config
     */
    public JdbcRepositoryWrapper(Vertx vertx, JsonObject config) {
        this.client = JDBCClient.create(vertx, config);
    }

    /**
     * Method to suit "add" and "exists" operations.
     *
     * @param params - Parameters of the Query.
     * @param sql - SQL String.
     * @param resultHandler - Async result handler.
     */
    protected void executeNoResult(JsonArray params, String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }

    /**
     * Method to suit "update" operations.
     *
     * @param params - Parameters of the Query.
     * @param sql - SQL String.
     * @param ret - Object Class to Update.
     * @param resultHandler - Async result handler.
     * @param <R> - Type of the Class Object.
     */
    protected <R> void execute(JsonArray params, String sql, R ret, Handler<AsyncResult<R>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(ret));
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }

    /**
     * Method to suit "get" of one item operations.
     *
     * @param param - Parameters to be insert in the query.
     * @param sql - The query to be execute.
     * @param <K> - Type of the Class Object.
     * @return - Promise to the execution of query.
     */
    protected <K> Future<Optional<JsonObject>> getOne(K param, String sql) {
        return getConnection().compose(connection -> {
            Promise<Optional<JsonObject>> promise = Promise.promise();
            connection.queryWithParams(sql, new JsonArray().add(param), r -> {
                if (r.succeeded()) {
                    List<JsonObject> resList = r.result().getRows();
                    if (resList == null || resList.isEmpty()) {
                        promise.complete(Optional.empty());
                    } else {
                        promise.complete(Optional.of(resList.get(0)));
                    }
                } else {
                    promise.fail(r.cause());
                }
                connection.close();
            });
            return promise.future();
        });
    }

    /**
     * Method that calculate the Page with a certain limit of items.
     *
     * @param page
     * @param limit - Limit of the items found.
     * @return int value
     */
    protected int calcPage(int page, int limit) {
        if (page <= 0)
            return 0;
        return limit * (page - 1);
    }

    /**
     * Method to suit "get" by Page multiple items operations.
     *
     * @param page
     * @param limit - Limit of the items found.
     * @param sql - The query to be execute.
     * @return - Promise to the execution of query.
     */
    protected Future<List<JsonObject>> getByPage(int page, int limit, String sql) {
        JsonArray params = new JsonArray().add(calcPage(page, limit)).add(limit);
        return getConnection().compose(connection -> {
            Promise<List<JsonObject>> promise = Promise.promise();
            connection.queryWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    promise.complete(r.result().getRows());
                } else {
                    promise.fail(r.cause());
                }
                connection.close();
            });
            return promise.future();
        });
    }

    /**
     * Method to suit "get" multiple items operations.
     *
     * @param param
     * @param sql - The query to be execute.
     * @return - Promise to the execution of query.
     */
    protected Future<List<JsonObject>> getMany(JsonArray param, String sql) {
        return getConnection().compose(connection -> {
            Promise<List<JsonObject>> promise = Promise.promise();
            connection.queryWithParams(sql, param, r -> {
                if (r.succeeded()) {
                    promise.complete(r.result().getRows());
                } else {
                    promise.fail(r.cause());
                }
                connection.close();
            });
            return promise.future();
        });
    }

    /**
     * Method to suit "get" of multiple items operations.
     *
     * @param sql - The query to be execute.
     * @return - Promise to the execution of query.
     */
    protected Future<List<JsonObject>> getAll(String sql) {
        return getConnection().compose(connection -> {
            Promise<List<JsonObject>> promise = Promise.promise();
            connection.query(sql, r -> {
                if (r.succeeded()) {
                    promise.complete(r.result().getRows());
                } else {
                    promise.fail(r.cause());
                }
                connection.close();
            });
            return promise.future();
        });
    }

    /**
     * Method to suit "delete" of one item operations.
     *
     * @param id - Id of the Item to be remove.
     * @param sql - The query to be execute.
     * @param resultHandler - Async result handler.
     * @param <K> - Type of the Class Object.
     */
    protected <K> void removeOne(K id, String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            JsonArray params = new JsonArray().add(id);
            connection.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }

    /**
     * Method to suit "delete" of multiple items operations.
     *
     * @param sql - The query to be execute.
     * @param resultHandler - Async result handler.
     */
    protected void removeAll(String sql, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.update(sql, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }

    /**
     * Method that generates async handler for SQLConnection.
     *
     * @param h1 - Async Handler
     * @param h2 - SQLConnection Handler
     * @param <R> - Type of the Class Object.
     * @return generated handler
     */
    protected <R> Handler<AsyncResult<SQLConnection>> connHandler(Handler<AsyncResult<R>> h1, Handler<SQLConnection> h2) {
        return conn -> {
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result();
                h2.handle(connection);
            } else {
                h1.handle(Future.failedFuture(conn.cause()));
            }
        };
    }

    /**
     * Method that get the connection of the MySQL Database.
     *
     * @return promise with the connection.
     */
    protected Future<SQLConnection> getConnection() {
        Promise<SQLConnection> promise = Promise.promise();
        client.getConnection(promise);
        return promise.future();
    }

}
