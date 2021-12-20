package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.engine.collections.ConcurrentPool;

@SuppressWarnings("ClassCanBeRecord")
public final class Combination {
    private final Class<? extends Component>[] componentTypes;
    private final ConcurrentPool.Tenant<LongEntity> tenant;

    @SafeVarargs
    public Combination(ConcurrentPool.Tenant<LongEntity> tenant, Class<? extends Component>... componentTypes) {
        this.tenant = tenant;
        this.componentTypes = componentTypes;
    }

    public LongEntity createEntity(Component... components) {
        long id = tenant.nextId();
        LongEntity entity = (tenant.register(id, new LongEntity(id, tenant)));
        //noinspection StatementWithEmptyBody
        if (componentTypes.length > 0) {
            //todo: store components
        }
        return entity;
    }
}
