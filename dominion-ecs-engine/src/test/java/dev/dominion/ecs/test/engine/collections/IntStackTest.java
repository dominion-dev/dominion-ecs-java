package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.IntStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class IntStackTest {
    @Test
    void pop() {
        try (IntStack stack = new IntStack(Integer.MIN_VALUE, 16)) {
            Assertions.assertEquals(IntStack.NULL_VALUE, stack.pop());
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
        try (IntStack stack = new IntStack(Integer.MIN_VALUE, 8)) {
            Assertions.assertTrue(stack.push(1));
            Assertions.assertTrue(stack.push(2));
            Assertions.assertTrue(stack.push(3));
            Assertions.assertEquals(3, stack.size());
        }
    }

    @Test
    void concurrentPush() throws InterruptedException {
        final int capacity = 1 << 10;
        final int limit = 1 << 20;
        try (IntStack stack = new IntStack(Integer.MIN_VALUE, capacity >>> 1)) {
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
        final var set = new HashSet<Integer>();
        try (IntStack stack = new IntStack(Integer.MIN_VALUE, capacity >>> 1)) {
            final ExecutorService pool = Executors.newFixedThreadPool(4);
            for (int i = 0; i < capacity; i++) {
                if (i % 10 == 0) {
                    pool.execute(() -> {
                        int value;
                        //noinspection StatementWithEmptyBody
                        while ((value = stack.pop()) == IntStack.NULL_VALUE) ;
                        set.add(value);
                    });
                }
                pool.execute(() -> stack.push(1));
            }
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            Assertions.assertEquals((int) (capacity * .9), stack.size());
            Assertions.assertEquals(1, set.size());
            Assertions.assertTrue(set.contains(1));
        }
    }
}
