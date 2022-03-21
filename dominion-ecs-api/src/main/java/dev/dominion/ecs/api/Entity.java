/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

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
