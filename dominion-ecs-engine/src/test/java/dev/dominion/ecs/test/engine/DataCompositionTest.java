package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.DataComposition;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataCompositionTest {

    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT);

    @Test
    void createEntityAtCompositionLevel() {
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.TEST)) {
            DataComposition composition =
                    new DataComposition(null, chunkedPool, null, ID_SCHEMA, LoggingSystem.Context.TEST);
            IntEntity entity = composition.createEntity(false);
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
        DataComposition composition = new DataComposition(null, null, classIndex, null
                , LoggingSystem.Context.TEST, C1.class
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
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            DataComposition composition = new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA
                    , LoggingSystem.Context.TEST
                    , C1.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(false, new C1(i));
            }
            var iterator = composition.selectT(C1.class, composition.getTenant().noItemIterator());
            int i = 0;
            while (iterator.hasNext()) {
                long id = iterator.next().id;
                Assertions.assertEquals(i++, id);
            }
            var iteratorE = composition.select(C1.class, composition.getTenant().iterator(), null);
            i = 0;
            while (iteratorE.hasNext()) {
                long id = iteratorE.next().comp().id;
                Assertions.assertEquals(i++, id);
            }
        }
    }

    @Test
    public void select2Comp() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        classIndex.addClass(C2.class);
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            DataComposition composition = new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA
                    , LoggingSystem.Context.VERBOSE_TEST
                    , C1.class, C2.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(false, new C1(i), new C2(i + 1));
            }
            ChunkedPool.Tenant<IntEntity> tenant = composition.getTenant();
            var iterator = composition.select(C1.class, C2.class, tenant.noItemIterator(), null);
            int i = 0;
            while (iterator.hasNext()) {
                var next = iterator.next();
                long id1 = next.comp1().id;
                long id2 = next.comp2().id;
                Assertions.assertEquals(i++, id1);
                Assertions.assertEquals(i, id2);
            }
            var iteratorE = composition.select(C1.class, C2.class, tenant.iterator(), null);
            i = 0;
            while (iteratorE.hasNext()) {
                var next = iteratorE.next();
                long id1 = next.comp1().id;
                long id2 = next.comp2().id;
                Assertions.assertEquals(i++, id1);
                Assertions.assertEquals(i, id2);
            }
        }
    }

    @Test
    void setEntityState() {
        ClassIndex classIndex = new ClassIndex();
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.TEST)) {
            DataComposition composition =
                    new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA, LoggingSystem.Context.TEST);
            IntEntity entity = composition.createEntity(false);
            entity.setState(State1.ONE);
            Assertions.assertEquals(entity.getStateChunk().getTenant(), composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.ONE)));
            Assertions.assertEquals(1, composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.ONE)).currentChunkSize());

            entity.setState(State1.TWO);
            Assertions.assertNotEquals(entity.getStateChunk().getTenant(), composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.ONE)));
            Assertions.assertEquals(entity.getStateChunk().getTenant(), composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.TWO)));
            Assertions.assertEquals(1, composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.TWO)).currentChunkSize());
            Assertions.assertEquals(0, composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.ONE)).currentChunkSize());

            entity.setState(null);
            Assertions.assertNull(entity.getStateChunk());
            Assertions.assertEquals(0, composition.getStateTenant(classIndex.getIndexKeyByEnum(State1.TWO)).currentChunkSize());
        }
    }

    enum State1 {
        ONE, TWO
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
