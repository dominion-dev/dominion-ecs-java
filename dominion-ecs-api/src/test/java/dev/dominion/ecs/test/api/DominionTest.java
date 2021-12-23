package dev.dominion.ecs.test.api;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class DominionTest {

    @Test
    void init() {
        Assertions.assertThrows(NoSuchElementException.class, Dominion::init);
        Assertions.assertEquals(MockDominion.class, Dominion.init("MockDominion").getClass());
    }

    @Test
    void createEntity() {
    }

    @Test
    void createEntityAs() {
    }

    @Test
    void destroyEntity() {
    }

    public static class MockDominion implements Dominion {

        @Override
        public Entity createEntity(Component... components) {
            return null;
        }

        @Override
        public Entity createEntityAs(Entity prefab, Component... components) {
            return null;
        }

        @Override
        public boolean destroyEntity(Entity entity) {
            return false;
        }

        @Override
        public void close() {
        }
    }
}
