package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompositionTest {

    @Test
    void createEntity() {
        ConcurrentPool<LongEntity> concurrentPool = new ConcurrentPool<>();
        try (ConcurrentPool.Tenant<LongEntity> tenant = concurrentPool.newTenant()) {
            Composition composition = new Composition(tenant, null);
            LongEntity entity = composition.createEntity();
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(composition, entity.getComposition());
            LongEntity entry = concurrentPool.getEntry(entity.getId());
            Assertions.assertNotNull(entry);
            Assertions.assertEquals(entity, entry);
        }
    }

    @Test
    void sortComponentsInPlaceByIndex() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        classIndex.addClass(C2.class);
        classIndex.addClass(C3.class);
        classIndex.addClass(C4.class);
        classIndex.addClass(C5.class);
        classIndex.addClass(C6.class);
        classIndex.addClass(C7.class);
        classIndex.addClass(C8.class);
        Composition composition = new Composition(null, classIndex
                , C1.class
                , C2.class
                , C3.class
                , C4.class
                , C5.class
                , C6.class
                , C7.class
                , C8.class
        );
        var c1 = new C1();
        var c2 = new C2();
        var c3 = new C3();
        var c4 = new C4();
        var c5 = new C5();
        var c6 = new C6();
        var c7 = new C7();
        var c8 = new C8();
        Assertions.assertArrayEquals(
                new Object[]{c1, c2, c3, c4, c5, c6, c7, c8}
                , composition.sortComponentsInPlaceByIndex(
                        new Object[]{c7, c3, c6, c8, c2, c5, c4, c1}
                )
        );
    }

    private static class C1 {
    }

    private static class C2 {
    }

    private static class C3 {
    }

    private static class C4 {
    }

    private static class C5 {
    }

    private static class C6 {
    }

    private static class C7 {
    }

    private static class C8 {
    }
}
