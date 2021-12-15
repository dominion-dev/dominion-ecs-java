package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Entity;

public final class LongEntity implements Entity {

    private long id;

    public LongEntity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
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
