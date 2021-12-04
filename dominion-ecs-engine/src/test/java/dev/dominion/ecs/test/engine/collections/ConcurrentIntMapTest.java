package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentIntMapTest {

    @Test
    public void put() {
        SparseIntMap<Integer> concurrentIntMap = new ConcurrentIntMap<>();
        Assertions.assertTrue(concurrentIntMap.isEmpty());
        concurrentIntMap.put(0, 0);
        Assertions.assertEquals(1, concurrentIntMap.size());
        concurrentIntMap.put(1, 1);
        Assertions.assertEquals(2, concurrentIntMap.size());
        concurrentIntMap.put(1, 2);
        Assertions.assertEquals(2, concurrentIntMap.size());
    }

    @Test
    public void get() {
        SparseIntMap<Integer> concurrentIntMap = new ConcurrentIntMap<>();
        concurrentIntMap.put(0, 0);
        concurrentIntMap.put(1, 1);
        Assertions.assertEquals(0, concurrentIntMap.get(0));
        Assertions.assertEquals(1, concurrentIntMap.get(1));
        concurrentIntMap.put(1, 2);
        Assertions.assertEquals(2, concurrentIntMap.get(1));
        Assertions.assertNull(concurrentIntMap.get(5));
    }

    @Test
    public void computeIfAbsent() {
        SparseIntMap<Integer> concurrentIntMap = new ConcurrentIntMap<>();
        Assertions.assertTrue(concurrentIntMap.isEmpty());
        concurrentIntMap.computeIfAbsent(0, k -> 0);
        Assertions.assertEquals(1, concurrentIntMap.size());
        Assertions.assertEquals(0, concurrentIntMap.get(0));
    }


    @Test
    public void concurrentPut() throws InterruptedException {
        final int capacity = 1 << 22;
        SparseIntMap<Integer> concurrentIntMap = new ConcurrentIntMap<>(capacity);
        final ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < capacity; i++) {
            int finalI = i;
            pool.execute(() -> concurrentIntMap.put(finalI, finalI));
        }
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        Assertions.assertEquals(capacity, concurrentIntMap.size());
    }

    @Test
    public void cloneTest() {
        SparseIntMap<Integer> concurrentIntMap = new ConcurrentIntMap<>();
        Assertions.assertTrue(concurrentIntMap.isEmpty());
        concurrentIntMap.put(0, 0);
        Assertions.assertFalse(concurrentIntMap.isEmpty());
        SparseIntMap<Integer> cloned = concurrentIntMap.clone();
        Assertions.assertFalse(cloned.isEmpty());
        Assertions.assertEquals(0, cloned.get(0));
        concurrentIntMap.put(1, 1);
        Assertions.assertEquals(1, concurrentIntMap.get(1));
        Assertions.assertNull(cloned.get(1));

    }
}
