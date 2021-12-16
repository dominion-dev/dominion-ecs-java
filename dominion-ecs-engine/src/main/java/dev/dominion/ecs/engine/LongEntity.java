package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;

public final class LongEntity implements Entity {

    private long id;
    private ConcurrentPool.Tenant<Object[]> tenant;

    public LongEntity(long id, ConcurrentPool.Tenant<Object[]> tenant) {
        this.id = id;
        this.tenant = tenant;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public ConcurrentPool.Tenant<Object[]> getTenant() {
        return tenant;
    }

    void setTenant(ConcurrentPool.Tenant<Object[]> tenant) {
        this.tenant = tenant;
    }

    @Override
    public void addComponents(Component... components) {

    }

    @Override
    public void removeComponents(Component... components) {

    }

    @Override
    public <S extends Enum<S>> void setState(S state) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }
}
