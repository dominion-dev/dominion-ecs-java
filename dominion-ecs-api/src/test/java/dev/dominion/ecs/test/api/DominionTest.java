package dev.dominion.ecs.test.api;

import dev.dominion.ecs.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

@SuppressWarnings("resource")
public class DominionTest {

    @Test
    void factoryCreate() {
        Assertions.assertThrows(NoSuchElementException.class, Dominion::create);
        Assertions.assertThrows(NoSuchElementException.class, () -> Dominion.create("test"));
        Assertions.assertThrows(NoSuchElementException.class, () -> Dominion.factory().create());
        Assertions.assertEquals(MockDominion.class, Dominion.factory("MockDominionFactory").create().getClass());
        Assertions.assertEquals("test", Dominion.factory("MockDominionFactory").create("test").getName());
    }

    @Test
    void createEntity() {
        Assertions.assertNull(Dominion.factory("MockDominionFactory").create().createEntity());
    }

    @Test
    void createEntityAs() {
        Assertions.assertNull(Dominion.factory("MockDominionFactory").create().createEntityAs(null));
    }

    @Test
    void deleteEntity() {
        Assertions.assertFalse(Dominion.factory("MockDominionFactory").create().deleteEntity(null));
    }

    public static class MockDominionFactory implements Dominion.Factory {
        @Override
        public Dominion create() {
            return create(null);
        }

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
        public Entity createPreparedEntity(Composition.OfTypes withValues) {
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
        public boolean modifyEntity(Composition.Modifier modifier) {
            return false;
        }

        @Override
        public Composition composition() {
            return null;
        }

        @Override
        public Scheduler createScheduler() {
            return null;
        }

        @Override
        public <T> Results<T> findCompositionsWith(Class<T> type) {
            return null;
        }

        @Override
        public <T1, T2> Results<Results.With2<T1, T2>> findCompositionsWith(Class<T1> type1, Class<T2> type2) {
            return null;
        }

        @Override
        public <T1, T2, T3> Results<Results.With3<T1, T2, T3>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4> Results<Results.With4<T1, T2, T3, T4>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4, T5> Results<Results.With5<T1, T2, T3, T4, T5>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4, T5, T6> Results<Results.With6<T1, T2, T3, T4, T5, T6>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            return null;
        }

        @Override
        public <T> Results<Results.With1<T>> findEntitiesWith(Class<T> type) {
            return null;
        }

        @Override
        public <T1, T2> Results<Results.With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2) {
            return null;
        }

        @Override
        public <T1, T2, T3> Results<Results.With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4> Results<Results.With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4, T5> Results<Results.With5<T1, T2, T3, T4, T5>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            return null;
        }

        @Override
        public <T1, T2, T3, T4, T5, T6> Results<Results.With6<T1, T2, T3, T4, T5, T6>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
