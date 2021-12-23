package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentLongStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentLongStackTest {

    @Test
    void pop() {
        try (ConcurrentLongStack stack = new ConcurrentLongStack(16)) {
            Assertions.assertEquals(Long.MIN_VALUE, stack.pop());
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
        try (ConcurrentLongStack stack = new ConcurrentLongStack(16)) {
            Assertions.assertTrue(stack.push(1));
            Assertions.assertTrue(stack.push(2));
            Assertions.assertFalse(stack.push(3));
            Assertions.assertEquals(2, stack.size());
        }
    }

    @Test
    void concurrentPush() throws InterruptedException {
        final int capacity = 1 << 20;
        try (ConcurrentLongStack stack = new ConcurrentLongStack(capacity * 8)) {
            final ExecutorService pool = Executors.newFixedThreadPool(4);
            for (int i = 0; i < capacity; i++) {
                pool.execute(() -> stack.push(1));
            }
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            Assertions.assertEquals(capacity, stack.size());
        }
    }

    @Test
    void concurrentPop() throws InterruptedException {
        final int capacity = 1 << 22;
        try (ConcurrentLongStack stack = new ConcurrentLongStack(capacity * 8)) {
            final ExecutorService pool = Executors.newFixedThreadPool(4);
            for (int i = 0; i < capacity; i++) {
                if (i % 10 == 0) {
                    pool.execute(() -> {
                        //noinspection StatementWithEmptyBody
                        while (stack.pop() == Long.MIN_VALUE) ;
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
