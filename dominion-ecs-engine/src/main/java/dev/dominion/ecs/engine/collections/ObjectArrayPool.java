/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class ObjectArrayPool {
    public static final int INITIAL_CAPACITY = 1 << 10;
    private final SparseIntMap<Stack> arraysByLengthMap = new ConcurrentIntMap<>();

    public Object[] push(Object[] objectArray) {
        int arrayLength = objectArray.length;
        var stack = arraysByLengthMap.get(arrayLength);
        if (stack == null) {
            stack = arraysByLengthMap.computeIfAbsent(arrayLength, k -> new Stack());
        }
        return stack.push(objectArray);
    }

    public Object[] pop(int arrayLength) {
        assert arrayLength > 0;
        var stack = arraysByLengthMap.get(arrayLength);
        Object[] objectArray = stack != null ? stack.pop() : null;
        return objectArray == null ? new Object[arrayLength] : objectArray;
    }

    public int size(int arrayLength) {
        var stack = arraysByLengthMap.get(arrayLength);
        return stack == null ? -1 : stack.size.get() + 1;
    }

    public static final class Stack {
        public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
        private final AtomicInteger size = new AtomicInteger(-1);
        private final StampedLock lock = new StampedLock();
        private int capacity = INITIAL_CAPACITY;
        private Reference<?>[] data = new Reference[capacity];

        public Object[] push(Object[] objectArray) {
            long stamp = lock.tryOptimisticRead();
            try {
                for (; ; ) {
                    if (stamp == 0L) {
                        stamp = lock.writeLock();
                        continue;
                    }
                    // possibly racy reads
                    int index;
                    if ((index = size.get()) < capacity - 1) {
                        boolean incremented = false;
                        while (!incremented && (index = size.get()) < capacity - 1) {
                            if (size.compareAndSet(index, index + 1)) {
                                incremented = true;
                                index++;
                            }
                        }
                        if (!incremented) {
                            stamp = lock.tryOptimisticRead();
                            continue;
                        }
                    } else {
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp == 0L) {
                            stamp = lock.writeLock();
                            continue;
                        }
                        // exclusive access
                        // ensure capacity
                        ensureCapacity();
                        index = size.incrementAndGet();
                    }
                    data[index] = new SoftReference<>(objectArray);
                    Arrays.fill(objectArray, null);
                    return objectArray;
                }
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
        }

        private void ensureCapacity() {
            capacity += capacity >> 1;
            if (capacity < 0 || capacity > SOFT_MAX_ARRAY_LENGTH) {
                throw new OutOfMemoryError(
                        "Required array length " + capacity + " is too large");
            }
            data = Arrays.copyOf(data, capacity);
        }

        public Object[] pop() {
            Object[] objectArray = null;
            int index;
            while (objectArray == null && (index = size.getAndDecrement()) > -1) {
                objectArray = (Object[]) data[index].get();
            }
            return objectArray;
        }
    }
}
