package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.DataComposition;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class DataCompositionTest {

    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT, ConfigSystem.DEFAULT_CHUNK_COUNT_BIT);

    @Test
    void createEntityAtCompositionLevel() {
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.TEST)) {
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant();
            DataComposition composition =
                    new DataComposition(null, tenant, null, ID_SCHEMA, LoggingSystem.Context.TEST);
            IntEntity entity = composition.createEntity(null, false);
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
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant(1);
            DataComposition composition = new DataComposition(null, tenant, classIndex, ID_SCHEMA
                    , LoggingSystem.Context.TEST
                    , C1.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(null, false, new C1(i));
            }
            Iterator<Results.With1<C1>> iterator = composition.select(C1.class, composition.getTenant().iterator());
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
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant(2);
            DataComposition composition = new DataComposition(null, tenant, classIndex, ID_SCHEMA
                    , LoggingSystem.Context.VERBOSE_TEST
                    , C1.class, C2.class);
            for (int i = 0; i < 1_000_000; i++) {
                composition.createEntity(null, false, new C1(i), new C2(i + 1));
            }
            Iterator<Results.With2<C1, C2>> iterator = composition.select(C1.class, C2.class, tenant.iterator());
            int i = 0;
            while (iterator.hasNext()) {
                Results.With2<C1, C2> next = iterator.next();
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
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant();
            DataComposition composition =
                    new DataComposition(null, tenant, classIndex, ID_SCHEMA, LoggingSystem.Context.TEST);
            IntEntity entity = composition.createEntity(null, false);
            composition.setEntityState(entity, State1.ONE);
            Assertions.assertTrue(composition.getStates().containsKey(DataComposition.calcIndexKey(State1.ONE, classIndex)));

            composition.setEntityState(entity, State1.TWO);
            Assertions.assertFalse(composition.getStates().containsKey(DataComposition.calcIndexKey(State1.ONE, classIndex)));
            Assertions.assertTrue(composition.getStates().containsKey(DataComposition.calcIndexKey(State1.TWO, classIndex)));
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity.getData().stateRoot());

            IntEntity entity2 = composition.createEntity(null, false);
            composition.setEntityState(entity2, State1.TWO);
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity2.getData().stateRoot());
            Assertions.assertEquals(entity, entity2.getPrev());
            Assertions.assertEquals(entity2, entity.getNext());
            Assertions.assertNull(entity.getData().stateRoot());

            composition.setEntityState(entity2, null);
            Assertions.assertNull(entity2.getData().stateRoot());
            Assertions.assertNull(entity2.getPrev());
            Assertions.assertNull(entity2.getNext());
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity.getData().stateRoot());

            composition.setEntityState(entity2, State1.TWO);
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity2.getData().stateRoot());
            IntEntity entity3 = composition.createEntity(null, false);
            composition.setEntityState(entity3, State1.TWO);
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity3.getData().stateRoot());
            Assertions.assertNull(entity2.getData().stateRoot());
            Assertions.assertEquals(entity2, entity.getNext());
            Assertions.assertEquals(entity, entity2.getPrev());
            Assertions.assertEquals(entity3, entity2.getNext());
            Assertions.assertEquals(entity2, entity3.getPrev());
            Assertions.assertNull(entity3.getNext());
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.TWO, classIndex), entity3.getData().stateRoot());

            composition.setEntityState(entity2, null);
            Assertions.assertNull(entity2.getPrev());
            Assertions.assertNull(entity2.getNext());
            Assertions.assertNull(entity2.getData().stateRoot());
            Assertions.assertEquals(entity3, entity.getNext());
            Assertions.assertEquals(entity, entity3.getPrev());
        }
    }

    @Test
    void concurrentSetEntityState() throws InterruptedException {
        int capacity = 1 << 20;
        int threadCount = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        IntEntity[] entities = new IntEntity[capacity];
        ClassIndex classIndex = new ClassIndex();
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant();
            DataComposition composition =
                    new DataComposition(null, tenant, classIndex, ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST);
            for (int i = 0; i < capacity; i++) {
                entities[i] = composition.createEntity(null, false);
            }
            AtomicInteger counter = new AtomicInteger(0);
            for (int i = 0; i < capacity; i++) {
                executorService.execute(() -> {
                    int idx = counter.getAndIncrement();
                    composition.setEntityState(entities[idx], State1.ONE);
                });
            }
            executorService.shutdown();
            Assertions.assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

            int count = 1;
            IntEntity entity = composition.getStates().get(DataComposition.calcIndexKey(State1.ONE, classIndex));
            System.out.println(entity);
            Assertions.assertEquals(DataComposition.calcIndexKey(State1.ONE, classIndex), entity.getData().stateRoot());
            IntEntity last = entity;
            while ((entity = (IntEntity) entity.getPrev()) != null) {
                Assertions.assertNull(entity.getData().stateRoot());
                Assertions.assertEquals(last, entity.getNext());
                last = entity;
                count++;
            }
            Assertions.assertEquals(capacity, count);
        }
    }

    @Test
    public void select1CompWithState() {
        ClassIndex classIndex = new ClassIndex();
        classIndex.addClass(C1.class);
        try (ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant(1);
            DataComposition composition = new DataComposition(null, tenant, classIndex, null
                    , LoggingSystem.Context.VERBOSE_TEST
                    , C1.class);
            int capacity = 1 << 16;
            for (int i = 0; i < capacity; i++) {
                IntEntity entity = composition.createEntity(null, false, new C1(i));
                composition.setEntityState(entity, State1.ONE);
            }
            IntEntity entity = composition.getStateRootEntity(DataComposition.calcIndexKey(State1.ONE, classIndex));
            Iterator<Results.With1<C1>> iterator = composition.select(C1.class, new DataComposition.StateIterator(entity));
            int count = 0;
            IntEntity last = null;
            while (iterator.hasNext()) {
                entity = (IntEntity) iterator.next().entity();
                Assertions.assertEquals(last, entity.getNext());
                last = entity;
                count++;
            }
            Assertions.assertEquals(capacity, count);
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
