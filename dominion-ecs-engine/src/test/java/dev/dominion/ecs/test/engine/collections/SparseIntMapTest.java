package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.SparseIntMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class SparseIntMapTest {

    @Test
    public void put() {
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>();
        Assertions.assertTrue(concurrentIntMap.isEmpty());
        Assertions.assertNull(concurrentIntMap.put(0, 0));
        Assertions.assertEquals(1, concurrentIntMap.size());
        Assertions.assertNull(concurrentIntMap.put(1, 1));
        Assertions.assertEquals(2, concurrentIntMap.size());
        Assertions.assertEquals(1, concurrentIntMap.put(1, 2));
        Assertions.assertEquals(2, concurrentIntMap.size());
    }

    @Test
    public void get() {
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>();
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
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>();
        Assertions.assertTrue(concurrentIntMap.isEmpty());
        concurrentIntMap.computeIfAbsent(0, k -> 0);
        Assertions.assertEquals(1, concurrentIntMap.size());
        Assertions.assertEquals(0, concurrentIntMap.get(0));
    }


    @Test
    public void concurrentPut() throws InterruptedException {
        final int capacity = 1 << 22;
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>(capacity);
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
    public void concurrentComputeIfNull() throws InterruptedException {
        final int capacity = 1 << 22;
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>(capacity);
        final ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < capacity; i++) {
            int finalI = i;
            pool.execute(() -> concurrentIntMap.computeIfAbsent(finalI, k -> finalI));
        }
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        Assertions.assertEquals(capacity, concurrentIntMap.size());
    }


    @Test
    public void values() {
        SparseIntMap<Integer> concurrentIntMap = new SparseIntMap<>();
        concurrentIntMap.put(0, 10);
        concurrentIntMap.put(1, 11);
        Integer[] values = concurrentIntMap.values();
        Assertions.assertArrayEquals(new Integer[]{10, 11}, values);
    }

    @Test
    public void sortedKeysHashCode() {
        SparseIntMap<Integer> map1 = new SparseIntMap<>();
        map1.put(1, 0);
        map1.put(2, 0);
        Assertions.assertEquals(8191 + 2, map1.sortedKeysHashCode());
        map1.put(3, 0);
        map1.put(4, 0);
        map1.put(5, 0);

        SparseIntMap<Integer> map2 = new SparseIntMap<>();
        map2.put(5, 0);
        map2.put(4, 0);
        map2.put(3, 0);
        map2.put(2, 0);
        map2.put(1, 0);
        Assertions.assertEquals(map2.sortedKeysHashCode(), map1.sortedKeysHashCode());
    }
}
