package fr.aquasys.swagger;

import scala.Tuple3;

import java.util.List;

public abstract class SwaggerDefinition {

    public abstract List<Tuple3<String, Class<?>, Boolean>> getDefinition();

    protected Tuple3<String, Class<?>, Boolean> getTuple(String name, Class<?> clazz, boolean required) {
        return new Tuple3<>(name, clazz, required);
    }

    protected Tuple3<String, Class<?>, Boolean> getTuple(String name, Class<?> clazz) {
        return getTuple(name, clazz, true);
    }
}
