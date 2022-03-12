package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.ConfigSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

class CompositionTest {

    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT);

    @Test
    void createEntity() {
        ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA);
        try (ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant()) {
            Composition composition = new Composition(null, tenant, null, null);
            IntEntity entity = composition.createEntity();
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(composition, entity.getComposition());
            IntEntity entry = chunkedPool.getEntry(entity.getId());
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
        Composition composition = new Composition(null, null, null, classIndex
                , C1.class
                , C2.class
                , C3.class
                , C4.class
                , C5.class
                , C6.class
                , C7.class
                , C8.class
        );
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        var c4 = new C4(0);
        var c5 = new C5(0);
        var c6 = new C6(0);
        var c7 = new C7(0);
        var c8 = new C8(0);
        Assertions.assertArrayEquals(
                new Object[]{c1, c2, c3, c4, c5, c6, c7, c8}
                , composition.sortComponentsInPlaceByIndex(
                        new Object[]{c7, c3, c6, c8, c2, c5, c4, c1}
                )
        );
    }

    @Test
    public void select1Comp() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA);
        try (ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant()) {
            Composition composition = new Composition(null, tenant, null, classIndex, C1.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(new C1(i));
            }
            Iterator<Results.Comp1<C1>> iterator = composition.select(C1.class);
            int i = 0;
            while (iterator.hasNext()) {
                long id = iterator.next().comp().id;
                Assertions.assertEquals(i++, id);
            }
        }
    }

    @Test
    public void select2Comp() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        classIndex.addClass(C2.class);
        ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA);
        try (ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant()) {
            Composition composition = new Composition(null, tenant, null, classIndex, C1.class, C2.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(new C1(i), new C2(i + 1));
            }
            Iterator<Results.Comp2<C1, C2>> iterator = composition.select(C1.class, C2.class);
            int i = 0;
            while (iterator.hasNext()) {
                Results.Comp2<C1, C2> next = iterator.next();
                long id1 = next.comp1().id;
                long id2 = next.comp2().id;
                Assertions.assertEquals(i++, id1);
                Assertions.assertEquals(i, id2);
            }
        }
    }

    record C1(int id) {
    }

    record C2(int id) {
    }

    record C3(int id) {
    }

    record C4(int id) {
    }

    record C5(int id) {
    }

    record C6(int id) {
    }

    record C7(int id) {
    }

    record C8(int id) {
    }
}
