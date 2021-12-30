package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;

public final class LongEntity implements Entity, ConcurrentPool.Identifiable {

    private long id;
    private Composition composition;
    private Component singleComponent;

    public LongEntity(long id, Composition composition) {
        this.id = id;
        this.composition = composition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    public Component getSingleComponent() {
        return singleComponent;
    }

    public LongEntity setSingleComponent(Component singleComponent) {
        this.singleComponent = singleComponent;
        return this;
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
