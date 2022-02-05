package dev.dominion.ecs.api;

public interface Entity {

    Entity add(Object... components);

    Entity remove(Object... components);

    boolean contains(Object component);

    <S extends Enum<S>> void setState(S state);

    void setEnabled(boolean enabled);
}
