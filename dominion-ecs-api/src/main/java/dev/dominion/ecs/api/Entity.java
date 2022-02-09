/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

public interface Entity {

    Entity add(Object... components);

    Entity remove(Object... components);

    boolean contains(Object component);

    <S extends Enum<S>> void setState(S state);

    void setEnabled(boolean enabled);
}
