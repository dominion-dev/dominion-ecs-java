/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.LoggingSystem;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class ObjectArrayPool {
    public static final int INITIAL_CAPACITY = 1 << 10;
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final SparseIntMap<Stack> arraysByLengthMap = new SparseIntMap<>();
    private final LoggingSystem.Context loggingContext;

    public ObjectArrayPool(LoggingSystem.Context loggingContext) {
        this.loggingContext = loggingContext;
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + getClass().getSimpleName()
                    )
            );
        }
    }

    public Object[] push(Object[] objectArray) {
        int arrayLength = objectArray.length;
        var stack = arraysByLengthMap.get(arrayLength);
        if (stack == null) {
            stack = arraysByLengthMap.computeIfAbsent(arrayLength, k -> new Stack(arrayLength, loggingContext));
        }
        return stack.push(objectArray);
    }

    public Object[] pop(int arrayLength) {
//        assert arrayLength > 0;
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
        private static final System.Logger LOGGER = LoggingSystem.getLogger();
        private final int arrayLength;
        private final AtomicInteger size = new AtomicInteger(-1);
        private final StampedLock lock = new StampedLock();
        private final LoggingSystem.Context loggingContext;
        private Reference<?>[] data = new Reference[INITIAL_CAPACITY];

        public Stack(int arrayLength, LoggingSystem.Context loggingContext) {
            this.arrayLength = arrayLength;
            this.loggingContext = loggingContext;
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                , "Creating " + this
                        )
                );
            }
        }

        public Object[] push(Object[] objectArray) {
            for (; ; ) {
                int index = size.get();
                if (index < data.length - 1) {
                    if (size.compareAndSet(index, index + 1)) {
                        data[++index] = new SoftReference<>(objectArray);
                        Arrays.fill(objectArray, null);
                        return objectArray;
                    }
                } else {
                    int currentCapacity = data.length;
                    long stamp = lock.writeLock();
                    try {
                        if (data.length == currentCapacity) {
                            ensureCapacity();
                        }
                    } finally {
                        lock.unlockWrite(stamp);
                    }
                }
            }
        }

        private void ensureCapacity() {
            int capacity = data.length + (data.length >> 1);
            if (capacity < 0 || capacity > SOFT_MAX_ARRAY_LENGTH) {
                throw new OutOfMemoryError(
                        "Required array length " + capacity + " is too large");
            }
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                , "Ensuring capacity " + this
                        )
                );
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

        @Override
        public String toString() {
            return "Stack={"
                    + "arrayLength=" + arrayLength
                    + ", capacity=" + data.length
                    + '}';
        }
    }
}
