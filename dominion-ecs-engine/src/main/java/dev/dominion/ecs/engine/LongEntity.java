/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;

public final class LongEntity implements Entity, ConcurrentPool.Identifiable {
    private long id;
    private Composition composition;
    private Object singleComponent;
    private Object[] components;
    private boolean isComponentArrayFromCache;

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

    public Object getSingleComponent() {
        return singleComponent;
    }

    public LongEntity setSingleComponent(Object singleComponent) {
        this.singleComponent = singleComponent;
        return this;
    }

    public Object[] getComponents() {
        return components;
    }

    public LongEntity setComponents(Object[] components) {
        this.components = components;
        return this;
    }

    @Override
    public Entity add(Object... components) {
        return composition.getRepository().addComponents(this, components);
    }

    @Override
    public Entity remove(Object... components) {
        return null;
    }

    @Override
    public boolean has(Class<?> componentType) {
        return composition.hasComponentType(componentType);
    }

    @Override
    public boolean contains(Object component) {
        int idx;
        return singleComponent != null ?
                singleComponent.equals(component) :
                components != null
                        && (idx = composition.fetchComponentIndex(component.getClass())) > -1
                        && components[idx].equals(component);
    }

    @Override
    public <S extends Enum<S>> void setState(S state) {
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    public boolean isComponentArrayFromCache() {
        return isComponentArrayFromCache;
    }

    void flagComponentArrayFromCache() {
        isComponentArrayFromCache = true;
    }
}
