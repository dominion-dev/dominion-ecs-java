/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

/**
 * An Entity identifies a single item and is represented as a unique integer value within a Dominion. Entities can contain
 * zero or more components that are POJOs with no behavior and can change components dynamically. Entities can be disabled
 * and re-enabled and can use a given Enum to optionally set a state.
 */
public interface Entity {

    Entity add(Object... components);

    Object remove(Object component);

    Object removeType(Class<?> componentType);

    boolean has(Class<?> componentType);

    boolean contains(Object component);

    <S extends Enum<S>> Entity setState(S state);

    boolean isEnabled();

    Entity setEnabled(boolean enabled);
}
