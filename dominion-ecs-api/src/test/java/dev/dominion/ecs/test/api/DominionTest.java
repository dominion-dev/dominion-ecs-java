package dev.dominion.ecs.test.api;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
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
        Assertions.assertNull(Dominion.init("MockDominion").createEntity());
    }

    @Test
    void createEntityAs() {
        Assertions.assertNull(Dominion.init("MockDominion").createEntityAs(null));
    }

    @Test
    void destroyEntity() {
        Assertions.assertFalse(Dominion.init("MockDominion").destroyEntity(null));
    }

    public static class MockDominion implements Dominion {

        @Override
        public Entity createEntity(Object... components) {
            return null;
        }

        @Override
        public Entity createEntityAs(Entity prefab, Object... components) {
            return null;
        }

        @Override
        public boolean destroyEntity(Entity entity) {
            return false;
        }

        @Override
        public <T> Results<Results.Comp1<T>> findComponents(Class<T> type) {
            return null;
        }

        @Override
        public <T1, T2> Results<Results.Comp2<T1, T2>> findComponents(Class<T1> type1, Class<T2> type2) {
            return null;
        }

        @Override
        public <T1, T2, T3> Results<Results.Comp3<T1, T2, T3>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4> Results<Results.Comp4<T1, T2, T3, T4>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
