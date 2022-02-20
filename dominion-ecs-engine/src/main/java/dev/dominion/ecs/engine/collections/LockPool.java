/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class LockPool {
    public final static LockPool INSTANCE = new LockPool();
    private final StampedLock[] data = new StampedLock[1 << 10];
    private final AtomicInteger size = new AtomicInteger(-1);

    private LockPool() {
    }

    public StampedLock push(StampedLock lock) {
        int idx;
        if ((idx = size.incrementAndGet()) >= data.length) {
            return lock;
        }
        return data[idx] = lock;
    }

    public StampedLock pop() {
        int index;
        while ((index = size.get()) > -1) {
            if (size.compareAndSet(index, index - 1)) {
                return data[index];
            }
        }
        return new StampedLock();
    }

    public int getSize() {
        return size.get() + 1;
    }

    public void clear() {
        Arrays.fill(data, null);
        size.set(-1);
    }
}
