package fr.aquasys.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.util.Json;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SwaggerHandler {

    private static SwaggerHandler instance;
    private Swagger swagger;
    private Map<String, Model> definitions = new HashMap<String, Model>();

    private Swagger getDefinition(Router router, String applicationTitle, String applicationDescription, String applicationVersion) {
        if (swagger == null) {
            this.swagger = new Swagger();
            this.swagger.setSwagger("2.0");
            getPaths(router.getRoutes().stream().filter(route -> route.getPath() != null && !route.getPath().equals("/")));
            Info info = new Info();
            info.title(applicationTitle).description(applicationDescription).version(applicationVersion);
            this.swagger.info(info);
            this.swagger.setDefinitions(definitions);
        }
        return swagger;
    }

    private void getPaths(Stream<Route> routes) {
        routes.forEach(route -> {
            Map<String, Path> paths = new HashMap<>();
            Router newRouter = getRouter(route);
            if (newRouter != null && newRouter instanceof SwaggerRouter) {
                SwaggerRouter swaggerRouter = (SwaggerRouter) newRouter;
                swaggerRouter.getSwaggerRoutes().stream()
                        .collect(Collectors.groupingBy(r -> r.getRoute().getPath(), Collectors.toList()))
                        .forEach((path, r) -> paths.put(route.getPath() + getSwaggerPath(path), getPath(r, swaggerRouter)));
                this.swagger.addTag(getTag(swaggerRouter));
                this.swagger.paths(paths);
            }
        });
    }

    private Tag getTag(SwaggerRouter swaggerRouter) {
        Tag tag = new Tag();
        tag.setName(swaggerRouter.getTag());
        return tag;
    }

    private Router getRouter(Route route) {
        Router router = null;
        try {
            Field routerField = route.getClass().getDeclaredField("contextHandler");
            routerField.setAccessible(true);
                /*
                 Get Handler
                  */
            Handler<RoutingContext> handler = (Handler<RoutingContext>) routerField.get(route);
            Field field = Arrays.asList(handler.getClass().getDeclaredFields())
                    .stream()
                    .filter(f -> f.getName().equals("arg$1"))
                    .findFirst()
                    .orElse(null);

            if (field != null) {
                field.setAccessible(true);
                router = (Router) field.get(handler);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return router;
    }

    private String getSwaggerPath(String path) {
        return Arrays.asList(path.split("/"))
                .stream()
                .map(p -> p.startsWith(":") ? "{" + p.replace(":", "") + "}" : p)
                .collect(Collectors.joining("/"));
    }

    private Path getPath(List<SwaggerRoute> routes, SwaggerRouter swaggerRouter) {
        Path path = new Path();
        routes.forEach(route -> {
            Operation operation = new Operation();
            /*
            Add Path parameters
             */
            List<Parameter> parameters = route.getPathParameters().stream().map(pathParam -> {
                return new PathParameter().name(pathParam.getKey()).type(pathParam.getValue().getSimpleName().toLowerCase());
            }).collect(Collectors.toList());

            /*
            Add Query parameters
             */
            parameters.addAll(route.getQueryParameters().stream().map(queryParam -> {
                return new QueryParameter().name(queryParam.getKey()).type(queryParam.getValue().getSimpleName().toLowerCase());
            }).collect(Collectors.toList()));

            /*
            Add Header parameters
             */
            parameters.addAll(route.getHeaderParameters().stream().map(header -> {
                return new HeaderParameter().name(header.getKey()).type(header.getValue().getSimpleName().toLowerCase());
            }).collect(Collectors.toList()));

            operation.setParameters(parameters);

            /*
            Add Responses
             */
            List<Pair<Integer, String>> responses = route.getResponses();
            responses.addAll(swaggerRouter.getResponses());
            operation.setResponses(responses
                    .stream()
                    .collect(Collectors.toMap(r -> r.getKey().toString(), r -> new Response().description(r.getValue()))));

            /*
            Add Body parameter if exist
             */
            String ref = putDefinition(route.getBodyParameter());
            if (ref != null) {
                operation.addParameter(new BodyParameter().name("body").schema(new RefModel(ref)));
            }

            path.set(route.getMethod(), operation);
        });
        swaggerRouter.getHeaderParameters().forEach(header -> {
            path.addParameter(new HeaderParameter().name(header.getKey()).type(header.getValue().getSimpleName().toLowerCase()));
        });


        return path;
    }

    private String putDefinition(Class<? extends SwaggerDefinition> definition) {
        String ref = null;
        if (definition != null) {
            try {
                SwaggerDefinition swaggerDefinition = definition.getConstructor().newInstance();
                ModelImpl model = new ModelImpl().type("object").xml(new Xml().name(definition.getSimpleName()));
                model.setProperties(swaggerDefinition.getDefinition().stream().collect(Collectors.toMap(prop -> prop._1(), prop -> {
                    Class<?> clazz = prop._2();
                    Property property = null;
                    if (clazz.isArray()) {
                        property = new ArrayProperty().items(PropertyBuilder.build(clazz.getComponentType().getSimpleName().toLowerCase(), null, null));
                    } else {
                        property = PropertyBuilder.build(clazz.getSimpleName().toLowerCase(), null, null);
                    }
                    property.setRequired(prop._3());
                    return property;
                })));
                definitions.put(definition.getSimpleName(), model);
                ref = definition.getSimpleName();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return ref;
    }

    public Handler<RoutingContext> getJson(Router mainRouter, String applicationTitle, String applicationDescription, String applicationVersion) {

        return r -> {
            try {
                r.response().end(Json.mapper().writeValueAsString(getDefinition(mainRouter, applicationTitle, applicationDescription, applicationVersion)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        };
    }

    public static SwaggerHandler getInstance() {
        if (instance == null) {
            instance = new SwaggerHandler();
        }
        return instance;
    }
}
