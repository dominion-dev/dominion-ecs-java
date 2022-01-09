package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public final class EntityRepository implements Dominion {

    private final LinkedCompositions compositions = new LinkedCompositions();

    @Override
    public Entity createEntity(Object... components) {
        final Composition composition = compositions.getOrCreate(components);
        return switch (components.length) {
            case 0 -> composition.createEntity();
            case 1 -> composition.createEntity(components[0]);
            default -> composition.createEntity(components);
        };
    }

    @Override
    public Entity createEntityAs(Entity prefab, Object... components) {
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
