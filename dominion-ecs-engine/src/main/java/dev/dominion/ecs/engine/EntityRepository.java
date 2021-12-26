package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;

public class EntityRepository implements Dominion {

    private final LinkedCompositions compositions = new LinkedCompositions();

    @Override
    public Entity createEntity(Component... components) {
        if (components.length == 0) {
            return compositions.getOrCreate().createEntity();
        }
        return null;
    }

    @Override
    public Entity createEntityAs(Entity prefab, Component... components) {
        return null;
    }

    @Override
    public boolean destroyEntity(Entity entity) {
        LongEntity longEntity = (LongEntity) entity;
        longEntity.getTenant().freeId(longEntity.getId());
        longEntity.setTenant(null);
        return true;
    }

    @Override
    public void close() {

    }
}
