package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;


public final class Combination {
    private final Class<? extends Component>[] componentTypes;
    private final int entryLength;
    private final ConcurrentPool.Tenant<Object[]> tenant;

    @SafeVarargs
    public Combination(ConcurrentPool.Tenant<Object[]> tenant, Class<? extends Component>... componentTypes) {
        this.tenant = tenant;
        this.componentTypes = componentTypes;
        entryLength = componentTypes.length + 1;
    }

    public Entity createEntity(Component... components) {
        return null;
    }
}
