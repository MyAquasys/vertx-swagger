package fr.aquasys.swagger;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.impl.RouterImpl;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SwaggerRouter extends RouterImpl {

    private final AtomicInteger orderSequence = new AtomicInteger();
    private List<SwaggerRoute> swaggerRoutes = new ArrayList<SwaggerRoute>();
    private List<Pair<String, Class<?>>> headerParameters;
    private List<Pair<Integer, String>> responses;

    public SwaggerRouter(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Route route(HttpMethod method, String path) {
        SwaggerRoute swaggerRoute = new SwaggerRoute(this, orderSequence.getAndIncrement(), method, path);
        swaggerRoutes.add(swaggerRoute);
        return swaggerRoute;
    }


    public SwaggerRoute get(String path) {
        return (SwaggerRoute) route(HttpMethod.GET, path);
    }

    public SwaggerRoute put(String path) {
        return (SwaggerRoute) route(HttpMethod.PUT, path);
    }

    public SwaggerRoute post(String path) {
        return (SwaggerRoute) route(HttpMethod.POST, path);
    }

    public SwaggerRoute delete(String path) {
        return (SwaggerRoute) route(HttpMethod.DELETE, path);
    }

    public List<SwaggerRoute> getSwaggerRoutes() {
        return swaggerRoutes;
    }

    public List<Pair<String, Class<?>>> getHeaderParameters() {
        if (headerParameters == null) {
            headerParameters = new ArrayList<>(1);
        }
        return headerParameters;
    }

    public void headerParameters(Pair<String, Class<?>>... headerParameters) {
        this.headerParameters = new ArrayList(Arrays.asList(headerParameters));
    }

    public void responses(Pair<Integer, String>... responses) {
        this.responses = new ArrayList(Arrays.asList(responses));
    }

    public List<Pair<Integer, String>> getResponses() {
        if (responses == null) {
            responses = new ArrayList<Pair<Integer, String>>(1);
        }
        return responses;
    }

    public static void init(Router mainRouter, Vertx vertx, String applicationTitle) {
        init(mainRouter, vertx, applicationTitle, null);
    }

    public static void init(Router mainRouter, Vertx vertx, String applicationTitle, String applicationDescription) {
        init(mainRouter, vertx, applicationTitle, applicationDescription, null);
    }

    public static void init(Router mainRouter, Vertx vertx, String applicationTitle, String applicationDescription, String applicationVersion) {
        Router swaggerRouter = Router.router(vertx);
        mainRouter.mountSubRouter("/swagger", swaggerRouter);
        swaggerRouter.get("/swagger-definition.json").handler(SwaggerHandler.getInstance().getJson(mainRouter, applicationTitle, applicationDescription, applicationVersion));
    }

}
