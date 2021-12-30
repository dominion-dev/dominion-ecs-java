package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.SparseIntMap;

public final class Composition {
    private final Class<? extends Component>[] componentTypes;
    private final ConcurrentPool.Tenant<LongEntity> tenant;
    private final SparseIntMap<Class<?>> componentIndexes = new ConcurrentIntMap<>();

    @SafeVarargs
    public Composition(ConcurrentPool.Tenant<LongEntity> tenant, Class<? extends Component>... componentTypes) {
        this.tenant = tenant;
        this.componentTypes = componentTypes;
    }

    public LongEntity createEntity(Component... components) {
        long id = tenant.nextId();
        LongEntity entity = (tenant.register(id, new LongEntity(id, this)));
        return switch (componentTypes.length) {
            case 0 -> entity;
            case 1 -> entity.setSingleComponent(components[0]);
            default -> null;
        };
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
