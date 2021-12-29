package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public class EntityRepository implements Dominion {

    private final LinkedCompositions compositions = new LinkedCompositions();

    @Override
    public Entity createEntity(Component... components) {
        return switch (components.length) {
            case 0 -> compositions.getOrCreate().createEntity();
            case 1 -> compositions.getOrCreate(components[0].getClass()).createEntity(components[0]);
            default -> null;
        };
    }

    @Override
    public Entity createEntityAs(Entity prefab, Component... components) {
        return null;
    }

    @Override
    public boolean destroyEntity(Entity entity) {
        LongEntity longEntity = (LongEntity) entity;
        return longEntity.getComposition().destroyEntity(longEntity);
    }

    @Override
    public void close() {
        compositions.close();
    }
}
