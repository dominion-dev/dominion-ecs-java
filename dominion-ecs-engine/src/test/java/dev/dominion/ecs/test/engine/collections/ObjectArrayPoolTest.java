package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ObjectArrayPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ObjectArrayPoolTest {

    @Test
    void pull() {
        ObjectArrayPool objectArrayPool = new ObjectArrayPool();
        Assertions.assertThrows(AssertionError.class, () -> objectArrayPool.pop(0));
        Object[] arrayObjects = objectArrayPool.pop(1);
        Assertions.assertEquals(1, arrayObjects.length);
        Object[] arrayObjects2 = objectArrayPool.pop(1);
        Assertions.assertNotEquals(arrayObjects2, arrayObjects);
    }

    @Test
    void push() {
        ObjectArrayPool pool = new ObjectArrayPool();
        Object[] arrayObjects = pool.pop(1);
        pool.push(arrayObjects);
        Assertions.assertEquals(1, pool.size(1));
        Object[] arrayObjects2 = pool.pop(1);
        Assertions.assertEquals(0, pool.size(1));
        Assertions.assertEquals(arrayObjects2, arrayObjects);
        arrayObjects = pool.pop(2);
        pool.push(arrayObjects);
        Assertions.assertEquals(1, pool.size(2));
        arrayObjects2 = pool.pop(2);
        Assertions.assertEquals(arrayObjects2, arrayObjects);
        Assertions.assertEquals(0, pool.size(2));
    }

    @Test
    void concurrentPush() throws InterruptedException {
        final int capacity = 1 << 20;
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        ObjectArrayPool objectArrayPool = new ObjectArrayPool();
        for (int i = 0; i < capacity; i++) {
            executorService.execute(() -> objectArrayPool.push(new Object[1]));
        }
        executorService.shutdown();
        Assertions.assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        Assertions.assertEquals(capacity, objectArrayPool.size(1));
    }
}
