package auth;

import common.BaseMicroserviceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import auth.api.AuthenticationAPIVerticle;
import auth.impl.AuthenticationServiceImpl;
import sd.microservices.common.BaseMicroserviceVerticle;

import static auth.AuthenticationService.SERVICE_ADDRESS;
import static auth.AuthenticationService.SERVICE_NAME;

public class AuthenticationVerticle extends BaseMicroserviceVerticle {

    private AuthenticationService authService;

    @Override
    public void start(Promise<Void> promise) throws Exception {
        super.start();
        authService = new AuthenticationServiceImpl(discovery);
        binder.setAddress(SERVICE_ADDRESS);
        binder.register(AuthenticationService.class, authService);
        // publish service and deploy REST verticle
        publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, AuthenticationService.class)
                .compose(servicePublished -> deployRestVerticle())
                .onComplete(promise);

    }

    /**
     * Deploys the AuthenticationApi Verticle
     *
     * @return Async result
     */
    private Future<Void> deployRestVerticle() {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(new AuthenticationAPIVerticle(authService),
                new DeploymentOptions().setConfig(config()),promise);
        return promise.future().map(r -> null);
    }
}


