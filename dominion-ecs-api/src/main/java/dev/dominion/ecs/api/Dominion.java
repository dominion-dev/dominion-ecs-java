package dev.dominion.ecs.api;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public interface Dominion extends AutoCloseable {

    static Dominion init() {
        return init("dev.dominion.ecs.engine.EntityRepository");
    }

    static Dominion init(String implementation) {
        return ServiceLoader
                .load(Dominion.class)
                .stream()
                .filter(p -> p.get().getClass().getName().equals(implementation))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Unable to load " + implementation))
                .get();
    }

    Entity createEntity(Component... components);

    Entity createEntityAs(Entity prefab, Component... components);

    boolean destroyEntity(Entity entity);
}
