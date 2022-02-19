/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.LockPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

class LockPoolTest {

    @Test
    void push() {
        LockPool lockPool = LockPool.INSTANCE;
        StampedLock lock = lockPool.pop();
        Assertions.assertEquals(0, lockPool.getSize());
        lockPool.push(lock);
        Assertions.assertEquals(1, lockPool.getSize());
    }

    @Test
    void pop() {
        LockPool lockPool = LockPool.INSTANCE;
        StampedLock lock = lockPool.pop();
        lockPool.push(lock);
        Assertions.assertEquals(1, lockPool.getSize());
        Assertions.assertEquals(lock, lockPool.pop());
        Assertions.assertEquals(0, lockPool.getSize());
    }

    @Test
    void concurrentAccess() throws InterruptedException {
        final int capacity = 1 << 24;
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        final LockPool lockPool = LockPool.INSTANCE;
        AtomicInteger nullError = new AtomicInteger(0);
        for (int i = 0; i < capacity; i++) {
            executorService.execute(() -> {
                StampedLock lock = lockPool.pop();
                if (lock == null) {
                    nullError.incrementAndGet();
                } else {
                    lockPool.push(lock);
                }
            });
        }
        executorService.shutdown();
        Assertions.assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        Assertions.assertEquals(0, nullError.get());
    }
}