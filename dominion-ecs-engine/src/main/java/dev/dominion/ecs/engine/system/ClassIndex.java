/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import sun.misc.Unsafe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClassIndex implements AutoCloseable {
    public final static int INT_BYTES_SHIFT = 2;
    public static final int DEFAULT_HASH_BIT = 20; // 1MB -> about 1K classes
    public static final int MIN_HASH_BIT = 14;
    public static final int MAX_HASH_BIT = 24;
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final Map<Object, Integer> controlMap = new ConcurrentHashMap<>(1 << 10);
    private final int hashBit;
    private final long memoryAddress;
    private final AtomicBoolean useFallbackMap = new AtomicBoolean(false);
    private final boolean fallbackMapEnabled;
    private final AtomicInteger atomicIndex = new AtomicInteger(0);
    private final int capacity;
    private int index = 1;
    private final ClassValue<Integer> fallbackMap = new ClassValue<>() {
        @Override
        protected Integer computeValue(Class<?> type) {
            return index++;
        }
    };

    public ClassIndex() {
        this(DEFAULT_HASH_BIT, true, LoggingSystem.Context.TEST);
    }

    public ClassIndex(int hashBit, boolean fallbackMapEnabled, LoggingSystem.Context loggingContext) {
        this.hashBit = Math.min(Math.max(hashBit, MIN_HASH_BIT), MAX_HASH_BIT);
        this.fallbackMapEnabled = fallbackMapEnabled;
        capacity = (1 << hashBit) << INT_BYTES_SHIFT;
        memoryAddress = unsafe.allocateMemory(capacity);
        unsafe.setMemory(memoryAddress, capacity, (byte) 0);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + this
                    )
            );
        }
    }

    private static long getIdentityAddress(long identityHashCode, long address) {
        return address + (identityHashCode << INT_BYTES_SHIFT);
    }

    public int getHashBit() {
        return hashBit;
    }

    public int addClass(Class<?> newClass) {
        return addObject(newClass);
    }

    public int addObject(Object newClass) {
        if (useFallbackMap.get()) {
            return fallbackMap.get((Class<?>) newClass);
        }
        int identityHashCode = capHashCode(System.identityHashCode(newClass), hashBit);
        long i = getIdentityAddress(identityHashCode, memoryAddress);
        int currentIndex = unsafe.getInt(i);
        if (currentIndex == 0) {
            int idx = fallbackMapEnabled ?
                    fallbackMap.get((Class<?>) newClass) :
                    atomicIndex.incrementAndGet();
            unsafe.putIntVolatile(null, i, idx);
            controlMap.put(newClass, idx);
            return idx;
        } else {
            if (!controlMap.containsKey(newClass)) {
                int idx = fallbackMap.get((Class<?>) newClass);
                useFallbackMap.set(true);
                return idx;
            }
        }
        return currentIndex;
    }

    public int getIndex(Class<?> klass) {
        return getObjectIndex(klass);
    }

    public int getObjectIndex(Object klass) {
        if (useFallbackMap.get()) {
            return fallbackMap.get((Class<?>) klass);
        }
        int identityHashCode = capHashCode(System.identityHashCode(klass), hashBit);
        return unsafe.getInt(getIdentityAddress(identityHashCode, memoryAddress));
    }

    public int getObjectIndexVolatile(Object klass) {
        if (useFallbackMap.get()) {
            return fallbackMap.get((Class<?>) klass);
        }
        int identityHashCode = capHashCode(System.identityHashCode(klass), hashBit);
        return unsafe.getIntVolatile(null, getIdentityAddress(identityHashCode, memoryAddress));
    }

    public int getIndexOrAddClass(Class<?> klass) {
        return getIndexOrAddObject(klass);
    }

    public int getIndexOrAddObject(Object klass) {
        int value = getObjectIndexVolatile(klass);
        if (value != 0) {
            return value;
        }
        return addObject(klass);
    }

    public int[] getIndexOrAddClassBatch(Class<?>[] classes) {
        int[] indexes = new int[classes.length];
        for (int i = 0; i < classes.length; i++) {
            indexes[i] = getIndexOrAddClass(classes[i]);
        }
        return indexes;
    }

    public int[] getIndexOrAddClassBatch(Object[] objects) {
        int[] indexes = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            indexes[i] = getIndexOrAddClass(objects[i].getClass());
        }
        return indexes;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public long longHashCode(Object[] objects) {
        boolean[] checkArray = new boolean[index + objects.length + 1];
        int min = Integer.MAX_VALUE, max = 0;
        for (int i = 0; i < objects.length; i++) {
            int value = getIndex(objects[i].getClass());
            value = value == 0 ? getIndexOrAddClass(objects[i].getClass()) : value;
            if (checkArray[value]) {
                throw new IllegalArgumentException("Duplicate object types are not allowed");
            }
            checkArray[value] = true;
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        long hashCode = 0;
        for (int i = min; i <= max; i++) {
            if (checkArray[i]) {
                hashCode = 31 * hashCode + i;
            }
        }
        return hashCode;
    }

    private int capHashCode(int hashCode, int hashBits) {
        return hashCode >> (32 - hashBits);
    }

    public int size() {
        return fallbackMapEnabled ?
                index - 1 :
                atomicIndex.get();
    }

    public void useUseFallbackMap() {
        useFallbackMap.set(true);
    }

    @Override
    public void close() {
        controlMap.clear();
        unsafe.freeMemory(memoryAddress);
    }

    @Override
    public String toString() {
        return "ClassIndex={"
                + "hashBit=" + hashBit
                + ", capacity=" + capacity + "|off-heap"
                + '}';
    }
}
