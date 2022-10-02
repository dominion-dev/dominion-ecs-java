package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.IdStack;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class IdStackTest {
    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT, ConfigSystem.DEFAULT_CHUNK_COUNT_BIT);

    @Test
    void pop() {
        try (IdStack stack = new IdStack(16, ID_SCHEMA, LoggingSystem.Context.TEST)) {
            Assertions.assertEquals(Integer.MIN_VALUE, stack.pop());
            stack.push(1);
            stack.push(2);
            Assertions.assertEquals(2, stack.size());
            Assertions.assertEquals(2, stack.pop());
            Assertions.assertEquals(1, stack.pop());
            Assertions.assertEquals(0, stack.size());
        }
    }

    @Test
    void push() {
        try (IdStack stack = new IdStack(8, ID_SCHEMA, LoggingSystem.Context.TEST)) {
            Assertions.assertTrue(stack.push(1));
            Assertions.assertTrue(stack.push(2));
            Assertions.assertFalse(stack.push(3));
            Assertions.assertEquals(2, stack.size());
        }
    }

    @Test
    void concurrentPush() throws InterruptedException {
        final int capacity = 1 << 10;
        final int limit = 1 << 20;
        try (IdStack stack = new IdStack(capacity * 8, ID_SCHEMA, LoggingSystem.Context.TEST)) {
            final ExecutorService pool = Executors.newFixedThreadPool(8);
            for (int i = 0; i < limit; i++) {
                pool.execute(() -> stack.push(1));
            }
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            Assertions.assertEquals(limit, stack.size());
        }
    }

    @Test
    void concurrentPop() throws InterruptedException {
        final int capacity = 1 << 22;
        try (IdStack stack = new IdStack(capacity * 8, ID_SCHEMA, LoggingSystem.Context.VERBOSE_TEST)) {
            final ExecutorService pool = Executors.newFixedThreadPool(4);
            for (int i = 0; i < capacity; i++) {
                if (i % 10 == 0) {
                    pool.execute(() -> {
                        //noinspection StatementWithEmptyBody
                        while (stack.pop() == Integer.MIN_VALUE) ;
                    });
                }
                pool.execute(() -> stack.push(1));
            }
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            Assertions.assertEquals((int) (capacity * .9), stack.size());
        }
    }
}
