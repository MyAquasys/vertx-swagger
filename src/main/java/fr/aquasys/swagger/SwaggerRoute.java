package fr.aquasys.swagger;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouteImpl;
import io.vertx.ext.web.impl.RouterImpl;
import javafx.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwaggerRoute implements Route {

    private RouteImpl route;
    private String method;
    private List<Pair<String, Class<?>>> queryParameters;
    private List<Pair<String, Class<?>>> pathParameters;
    private List<Pair<String, Class<?>>> headerParameters;
    private List<Pair<Integer, String>> responses;
    private Class<? extends SwaggerDefinition> bodyParameter;


    public SwaggerRoute(RouterImpl router, int order, HttpMethod method, String path) {
        try {
            Constructor c = RouteImpl.class.getDeclaredConstructor(RouterImpl.class, int.class, HttpMethod.class, String.class);
            c.setAccessible(true);
            this.route = (RouteImpl) c.newInstance(router, order, method, path);
            this.method = method.name().toLowerCase();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public SwaggerRoute pathParameters(Pair<String, Class<?>>... pathParameters) {
        this.pathParameters = new ArrayList(Arrays.asList(pathParameters));
        return this;
    }

    public SwaggerRoute queryParameters(Pair<String, Class<?>>... queryParameters) {
        this.queryParameters = new ArrayList(Arrays.asList(queryParameters));
        return this;
    }

    public SwaggerRoute bodyParameter(Class<? extends SwaggerDefinition> bodyParameter) {
        this.bodyParameter = bodyParameter;
        return this;
    }

    public SwaggerRoute headerParameters(Pair<String, Class<?>>... headerParameters) {
        this.headerParameters = new ArrayList(Arrays.asList(headerParameters));
        return this;
    }

    public SwaggerRoute responses(Pair<Integer, String>... responses) {
        this.responses = new ArrayList(Arrays.asList(responses));
        return this;
    }

    public List<Pair<String, Class<?>>> getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = new ArrayList<Pair<String, Class<?>>>(1);
        }
        return queryParameters;
    }

    public List<Pair<String, Class<?>>> getPathParameters() {
        if (pathParameters == null) {
            pathParameters = new ArrayList<Pair<String, Class<?>>>(1);
        }
        return pathParameters;
    }

    public Class<? extends SwaggerDefinition> getBodyParameter() {
        return bodyParameter;
    }

    public List<Pair<Integer, String>> getResponses() {
        if (responses == null) {
            responses = new ArrayList<Pair<Integer, String>>(1);
        }
        return responses;
    }

    public List<Pair<String, Class<?>>> getHeaderParameters() {
        if (headerParameters == null) {
            headerParameters = new ArrayList<Pair<String, Class<?>>>(1);
        }
        return headerParameters;
    }

    public RouteImpl getRoute() {
        return route;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public Route method(HttpMethod method) {
        return route.method(method);
    }

    @Override
    public Route path(String path) {
        return route.path(path);
    }

    @Override
    public Route pathRegex(String path) {
        return route.pathRegex(path);
    }

    @Override
    public Route produces(String contentType) {
        return route.produces(contentType);
    }

    @Override
    public Route consumes(String contentType) {
        return route.consumes(contentType);
    }

    @Override
    public Route order(int order) {
        return route.order(order);
    }

    @Override
    public Route last() {
        return route.last();
    }

    @Override
    public Route handler(Handler<RoutingContext> requestHandler) {
        return route.handler(requestHandler);
    }

    @Override
    public Route blockingHandler(Handler<RoutingContext> requestHandler) {
        return route.blockingHandler(requestHandler);
    }

    @Override
    public Route blockingHandler(Handler<RoutingContext> requestHandler, boolean ordered) {
        return route.blockingHandler(requestHandler, ordered);
    }

    @Override
    public Route failureHandler(Handler<RoutingContext> failureHandler) {
        return route.failureHandler(failureHandler);
    }

    @Override
    public Route remove() {
        return route.remove();
    }

    @Override
    public Route disable() {
        return route.disable();
    }

    @Override
    public Route enable() {
        return route.enable();
    }

    @Override
    public Route useNormalisedPath(boolean useNormalisedPath) {
        return route.useNormalisedPath(useNormalisedPath);
    }

    @Override
    public String getPath() {
        return route.getPath();
    }
}
