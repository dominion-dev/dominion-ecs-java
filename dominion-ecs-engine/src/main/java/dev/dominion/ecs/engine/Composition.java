package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.engine.collections.ConcurrentPool;

@SuppressWarnings("ClassCanBeRecord")
public final class Composition {
    private final Class<? extends Component>[] componentTypes;
    private final ConcurrentPool.Tenant<LongEntity> tenant;

    @SafeVarargs
    public Composition(ConcurrentPool.Tenant<LongEntity> tenant, Class<? extends Component>... componentTypes) {
        this.tenant = tenant;
        this.componentTypes = componentTypes;
    }

    public LongEntity createEntity(Component... components) {
        long id = tenant.nextId();
        LongEntity entity = (tenant.register(id, new LongEntity(id, this)));
        //noinspection StatementWithEmptyBody
        if (componentTypes.length > 0) {

        }
        return entity;
    }

    public boolean destroyEntity(LongEntity entity) {
        tenant.freeId(entity.getId());
        entity.setComposition(null);
        return true;
    }

    public Class<? extends Component>[] getComponentTypes() {
        return componentTypes;
    }

    public ConcurrentPool.Tenant<LongEntity> getTenant() {
        return tenant;
    }
}
