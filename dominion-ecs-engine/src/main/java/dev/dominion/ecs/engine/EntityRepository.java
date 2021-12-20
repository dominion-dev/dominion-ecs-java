package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public class EntityRepository implements Dominion {
    @Override
    public Entity createEntity(Component... components) {
        return null;
    }

    @Override
    public Entity createEntityAs(Entity prefab, Component... components) {
        return null;
    }

    @Override
    public boolean destroyEntity(Entity entity) {
        return false;
    }

    @Override
    public void close(){

    }
}
