package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.IntEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class IntEntityTest {

    @Test
    void add() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            Assertions.assertNull(entity.getComponents());
            entity.add(c1);
            Assertions.assertEquals(c1, entity.getComponents()[0]);
            entity.add(c2);
            Assertions.assertArrayEquals(new Object[]{c1, c2}, entity.getComponents());
        }
    }

    @Test
    void concurrentAdd() throws InterruptedException {
        int capacity = 1 << 16;
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        Entity[] entities = new Entity[capacity];
        CountDownLatch[] latches = new CountDownLatch[capacity * 2];

        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            var c3 = new C3(0);
            var c4 = new C4(0);
            var c5 = new C5(0);

            for (int i = 0; i < capacity; i++) {
                entities[i] = entityRepository.createEntity(c1, c2);
                latches[i * 2] = new CountDownLatch(threadCount);
                latches[i * 2 + 1] = new CountDownLatch(threadCount);
            }
            AtomicInteger counter = new AtomicInteger(0);
            for (int i = 0; i < capacity; i++) {
                executorService.execute(() -> {
                    int idx = counter.get();
                    int latchIdx = idx * 2;
                    entities[idx].add(c3);
                    try {
                        latches[latchIdx].countDown();
                        latches[latchIdx].await();
                        latches[latchIdx + 1].countDown();
                        latches[latchIdx + 1].await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                executorService.execute(() -> {
                    int idx = counter.get();
                    int latchIdx = idx * 2;
                    entities[idx].add(c4);
                    try {
                        latches[latchIdx].countDown();
                        latches[latchIdx].await();
                        latches[latchIdx + 1].countDown();
                        latches[latchIdx + 1].await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                executorService.execute(() -> {
                    int idx = counter.get();
                    int latchIdx = idx * 2;
                    entities[idx].add(c5);
                    try {
                        latches[latchIdx].countDown();
                        latches[latchIdx].await();
                        counter.incrementAndGet();
                        latches[latchIdx + 1].countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            executorService.shutdown();
            Assertions.assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

            for (int i = 0; i < capacity; i++) {
                Assertions.assertArrayEquals(new Object[]{c1, c2, c3, c4, c5},
                        Arrays.stream(((IntEntity) entities[i]).getComponents())
                                .sorted(Comparator.comparing(comp -> comp.getClass().getName())).toArray());
            }
        }
    }

    @Test
    void remove() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            var c3 = new C3(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity(c1, c2, c3);
            Assertions.assertEquals(c2, entity.remove(c2));
            Assertions.assertArrayEquals(new Object[]{c1, c3}, entity.getComponents());
            Assertions.assertEquals(c1, entity.remove(c1));
            Assertions.assertEquals(c3, entity.getComponents()[0]);
            Assertions.assertEquals(c3, entity.remove(c3));
            Assertions.assertNull(entity.getComponents());
        }
    }

    @Test
    void addAndRemove() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            var c3 = new C3(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity(c1, c2, c3);
            Composition compositionV3 = entity.getComposition();
            entity.remove(c2);
            Assertions.assertArrayEquals(new Object[]{c1, c3}, entity.getComponents());
            Composition compositionV2 = entity.getComposition();
            entity.remove(c1);
            Assertions.assertEquals(c3, entity.getComponents()[0]);
            Composition compositionV1 = entity.getComposition();
            entity.remove(c3);
            Assertions.assertNull(entity.getComponents());
            entity.add(c3);
            Assertions.assertEquals(c3, entity.getComponents()[0]);
            Assertions.assertEquals(compositionV1, entity.getComposition());
            entity.add(c2);
            Assertions.assertArrayEquals(new Object[]{c3, c2}, entity.getComponents());
            Assertions.assertNotEquals(compositionV2, entity.getComposition());
            entity.remove(c2);
            entity.add(c1);
            Assertions.assertEquals(compositionV2, entity.getComposition());
            entity.add(c2);
            Assertions.assertEquals(compositionV3, entity.getComposition());
        }
    }

    @Test
    void has() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            Assertions.assertFalse(entity.has(C1.class));
            IntEntity entity2 = (IntEntity) entityRepository.createEntity(c1);
            Assertions.assertTrue(entity2.has(C1.class));
            entity.add(c1);
            Assertions.assertTrue(entity.has(C1.class));
            entity.add(c2);
            Assertions.assertTrue(entity.has(C1.class));
            Assertions.assertTrue(entity.has(C2.class));
        }
    }

    @Test
    void contains() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            Assertions.assertFalse(entity.contains(c1));
            IntEntity entity2 = (IntEntity) entityRepository.createEntity(c1);
            Assertions.assertTrue(entity2.contains(c1));
            entity.add(c1);
            Assertions.assertTrue(entity.contains(c1));
            entity.add(c2);
            Assertions.assertTrue(entity.contains(c1));
            Assertions.assertTrue(entity.contains(c2));
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
}
