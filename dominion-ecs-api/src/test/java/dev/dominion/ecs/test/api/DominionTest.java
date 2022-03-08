package dev.dominion.ecs.test.api;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class DominionTest {

    @Test
    void factoryCreate() {
        Assertions.assertThrows(NoSuchElementException.class, () -> Dominion.factory().create(""));
        Assertions.assertEquals(MockDominion.class, Dominion.factory("MockDominionFactory").create("").getClass());
        Assertions.assertEquals("TEST", Dominion.factory("MockDominionFactory").create("TEST").getName());
    }

    @Test
    void createEntity() {
        Assertions.assertNull(Dominion.factory("MockDominionFactory").create("").createEntity());
    }

    @Test
    void createEntityAs() {
        Assertions.assertNull(Dominion.factory("MockDominionFactory").create("").createEntityAs(null));
    }

    @Test
    void deleteEntity() {
        Assertions.assertFalse(Dominion.factory("MockDominionFactory").create("").deleteEntity(null));
    }

    public static class MockDominionFactory implements Dominion.Factory {
        @Override
        public Dominion create(String name) {
            return new MockDominion(name);
        }
    }

    public record MockDominion(String name) implements Dominion {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Entity createEntity(Object... components) {
            return null;
        }

        @Override
        public Entity createEntityAs(Entity prefab, Object... components) {
            return null;
        }

        @Override
        public boolean deleteEntity(Entity entity) {
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
        public <T1, T2, T3, T4, T5> Results<Results.Comp5<T1, T2, T3, T4, T5>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4, T5, T6> Results<Results.Comp6<T1, T2, T3, T4, T5, T6>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
