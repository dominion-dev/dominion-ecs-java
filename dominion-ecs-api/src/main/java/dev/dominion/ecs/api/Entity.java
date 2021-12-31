package dev.dominion.ecs.api;

public interface Entity {

    void addComponents(Object... components);

    void removeComponents(Object... components);

    <S extends Enum<S>> void setState(S state);

    void setEnabled(boolean enabled);
}
