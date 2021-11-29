package dev.dominion.ecs.api;

public interface Entity {

    void addComponents(Component... components);

    void removeComponents(Component... components);

    <S extends Enum<S>> void setState(S state);

    void setEnabled(boolean enabled);
}
