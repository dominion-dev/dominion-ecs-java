package dev.dominion.ecs.engine;

import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import dev.dominion.ecs.engine.system.ClassIndex;

public final class Composition {
    private final Class<?>[] componentTypes;
    private final ConcurrentPool.Tenant<LongEntity> tenant;
    private final ClassIndex classIndex;
    private final SparseIntMap<Integer> componentIndexes;

    public Composition(ConcurrentPool.Tenant<LongEntity> tenant, ClassIndex classIndex, Class<?>... componentTypes) {
        this.tenant = tenant;
        this.classIndex = classIndex;
        this.componentTypes = componentTypes;
        if (componentTypes.length > 1) {
            componentIndexes = new ConcurrentIntMap<>();
            for (int i = 0; i < componentTypes.length; i++) {
                componentIndexes.put(classIndex.getIndex(componentTypes[i]), i);
            }
        } else {
            componentIndexes = null;
        }
    }

    public Object[] orderComponentsInPlaceByIndex(Object[] components) {
        int newIdx;
        for (int i = 0; i < components.length; i++) {
            newIdx = componentIndexes.get(classIndex.getIndex(components[i].getClass()));
            if (newIdx != i) {
                swapComponents(components, i, newIdx);
            }
        }
        newIdx = componentIndexes.get(classIndex.getIndex(components[0].getClass()));
        if (newIdx > 0) {
            swapComponents(components, 0, newIdx);
        }
        return components;
    }

    private void swapComponents(Object[] components, int i, int newIdx) {
        Object temp = components[newIdx];
        components[newIdx] = components[i];
        components[i] = temp;
    }

    public LongEntity createEntity(Object... components) {
        long id = tenant.nextId();
        LongEntity entity = (tenant.register(id, new LongEntity(id, this)));
        return switch (componentTypes.length) {
            case 0 -> entity;
            case 1 -> entity.setSingleComponent(components[0]);
            default -> entity.setComponents(orderComponentsInPlaceByIndex(components));
        };
    }

    public boolean destroyEntity(LongEntity entity) {
        tenant.freeId(entity.getId());
        entity.setComposition(null);
        entity.setSingleComponent(null);
        entity.setComponents(null);
        return true;
    }

    public Class<?>[] getComponentTypes() {
        return componentTypes;
    }

    public ConcurrentPool.Tenant<LongEntity> getTenant() {
        return tenant;
    }
}
