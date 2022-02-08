package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ObjectArrayCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ObjectArrayCacheTest {

    @Test
    void pull() {
        ObjectArrayCache objectArrayCache = new ObjectArrayCache();
        Assertions.assertThrows(AssertionError.class, () -> objectArrayCache.pop(0));
        Object[] arrayObjects = objectArrayCache.pop(1);
        Assertions.assertEquals(1, arrayObjects.length);
        Object[] arrayObjects2 = objectArrayCache.pop(1);
        Assertions.assertNotEquals(arrayObjects2, arrayObjects);
    }

    @Test
    void push() {
        ObjectArrayCache cache = new ObjectArrayCache();
        Object[] arrayObjects = cache.pop(1);
        cache.push(arrayObjects);
        Assertions.assertEquals(1, cache.size(1));
        Object[] arrayObjects2 = cache.pop(1);
        Assertions.assertEquals(0, cache.size(1));
        Assertions.assertEquals(arrayObjects2, arrayObjects);
        arrayObjects = cache.pop(2);
        cache.push(arrayObjects);
        Assertions.assertEquals(1, cache.size(2));
        arrayObjects2 = cache.pop(2);
        Assertions.assertEquals(arrayObjects2, arrayObjects);
        Assertions.assertEquals(0, cache.size(2));
    }

    @Test
    void concurrentPush() throws InterruptedException {
        final int capacity = 1 << 20;
        final ExecutorService pool = Executors.newFixedThreadPool(4);
        ObjectArrayCache cache = new ObjectArrayCache();
        for (int i = 0; i < capacity; i++) {
            pool.execute(() -> cache.push(new Object[1]));
        }
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        Assertions.assertEquals(capacity, cache.size(1));
    }
}
