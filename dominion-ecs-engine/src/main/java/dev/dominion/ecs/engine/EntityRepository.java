package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public final class EntityRepository implements Dominion {

    private final LinkedCompositions compositions = new LinkedCompositions();

    private static Class<?>[] getClasses(Object[] components) {
        Class<?>[] classes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            classes[i] = components[i].getClass();
        }
        return classes;
    }

    @Override
    public Entity createEntity(Object... components) {
        return switch (components.length) {
            case 0 -> compositions.getOrCreate().createEntity();
            case 1 -> compositions.getOrCreate(components[0].getClass()).createEntity(components[0]);
            default -> compositions.getOrCreate(getClasses(components)).createEntity(components);
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
