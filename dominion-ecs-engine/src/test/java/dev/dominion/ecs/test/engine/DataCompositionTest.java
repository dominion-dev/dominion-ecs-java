package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.DataComposition;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.Config;
import dev.dominion.ecs.engine.system.Logging;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataCompositionTest {

    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(Config.DominionSize.MEDIUM.chunkBit());

    @Test
    void createEntityAtCompositionLevel() {
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, Logging.Context.TEST)) {
            DataComposition composition =
                    new DataComposition(null, chunkedPool, null, ID_SCHEMA, Logging.Context.TEST);
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
                , Logging.Context.TEST, C1.class
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
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, Logging.Context.STRESS_TEST)) {
            DataComposition composition = new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA
                    , Logging.Context.TEST
                    , C1.class);
            int capacity = 1 << 20;
            for (int i = 0; i < capacity; i++) {
                composition.createEntity(false, new C1(i));
            }
            var iterator = composition.selectT(C1.class, composition.getTenant().noItemIterator());
            long lastId = 0;
            while (iterator.hasNext()) {
                long id = iterator.next().id;
                Assertions.assertTrue((id + 1) % ID_SCHEMA.chunkCapacity() == 0 || lastId - 1 == id);
                lastId = id;
            }
            var iteratorE = composition.select(C1.class, composition.getTenant().iterator(), null);
            lastId = 0;
            while (iteratorE.hasNext()) {
                long id = iteratorE.next().comp().id;
                Assertions.assertTrue((id + 1) % ID_SCHEMA.chunkCapacity() == 0 || lastId - 1 == id);
                lastId = id;
            }
        }
    }

    @Test
    public void select2Comp() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        classIndex.addClass(C2.class);
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, Logging.Context.STRESS_TEST)) {
            DataComposition composition = new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA
                    , Logging.Context.STRESS_TEST
                    , C1.class, C2.class);
            int capacity = 1 << 20;
            for (int i = 0; i < capacity; i++) {
                composition.createEntity(false, new C1(i), new C2(i + 1));
            }
            ChunkedPool.Tenant<IntEntity> tenant = composition.getTenant();
            var iterator = composition.select(C1.class, C2.class, tenant.noItemIterator(), null);
            long lastId1 = 0, lastId2 = 0;
            while (iterator.hasNext()) {
                var next = iterator.next();
                long id1 = next.comp1().id;
                long id2 = next.comp2().id;
                Assertions.assertTrue((id1 + 1) % ID_SCHEMA.chunkCapacity() == 0 || lastId1 - 1 == id1);
                Assertions.assertTrue((id2) % ID_SCHEMA.chunkCapacity() == 0 || lastId2 - 1 == id2);
                lastId1 = id1;
                lastId2 = id2;
            }
            var iteratorE = composition.select(C1.class, C2.class, tenant.iterator(), null);
            lastId1 = 0;
            lastId2 = 0;
            while (iteratorE.hasNext()) {
                var next = iteratorE.next();
                long id1 = next.comp1().id;
                long id2 = next.comp2().id;
                Assertions.assertTrue((id1 + 1) % ID_SCHEMA.chunkCapacity() == 0 || lastId1 - 1 == id1);
                Assertions.assertTrue((id2) % ID_SCHEMA.chunkCapacity() == 0 || lastId2 - 1 == id2);
                lastId1 = id1;
                lastId2 = id2;
            }
        }
    }

    @Test
    void setEntityState() {
        ClassIndex classIndex = new ClassIndex();
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, Logging.Context.TEST)) {
            DataComposition composition =
                    new DataComposition(null, chunkedPool, classIndex, ID_SCHEMA, Logging.Context.TEST);
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
