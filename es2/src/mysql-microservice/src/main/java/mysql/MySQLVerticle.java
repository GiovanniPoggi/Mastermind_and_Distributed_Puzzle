package mysql;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import common.BaseMicroserviceVerticle;
import mysql.api.MySQLAPIVerticle;
import mysql.impl.JdbcAccountServiceImpl;

import static mysql.AccountService.SERVICE_ADDRESS;

/**
 * Class representing a verticle which publishes the User service.
 */
public class MySQLVerticle extends BaseMicroserviceVerticle {

    /**
     * Field that create AccountService instance.
     */
    private AccountService accountService;

    /**
     * Method that create the service instance.
     *
     * @param promise
     * @throws Exception - if the calls goes wrong.
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        this.accountService = new JdbcAccountServiceImpl(vertx, config()) {
        };

        // create the service instance
        accountService = new JdbcAccountServiceImpl(vertx, config());
        binder.setAddress(AccountService.SERVICE_ADDRESS);
        binder.register(AccountService.class, accountService);
        // publish the service and REST endpoint in the discovery infrastructure
        initDatabaseUser()
                .compose(databaseOkay -> publishEventBusService(AccountService.SERVICE_NAME, SERVICE_ADDRESS, AccountService.class))
                .compose(servicePublished -> deployRestVerticle())
                .onComplete(promise);
    }

    /**
     * Method that create User Database.
     *
     * @return the promise of creation.
     */
    private Future<Void> initDatabaseUser() {
        Promise<Void> promise = Promise.promise();
        accountService.createUserDatabase(promise);
        return promise.future();
    }

    /**
     * Method that deploy the Rest Verticle.
     *
     * @return the promise of deploy, asynchronous result.
     */
    private Future<Void> deployRestVerticle() {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(new MySQLAPIVerticle(accountService),
                new DeploymentOptions().setConfig(config()),
                promise);
        return promise.future().map(r -> null);
    }
}

