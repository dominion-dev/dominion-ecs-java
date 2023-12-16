/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import dev.dominion.ecs.engine.collections.IndexedObjectCache;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ClassIndex class is the cornerstone of Dominion.
 * In less than 2 nanoseconds, this implementation can provide a progressive int value for each different component type.
 * This allows you to use the blazing fast counting sort algorithm - with O(n+k) time complexity - to sort component
 * types (even finding duplicates) and implement a very efficient {@link IndexKey} to represent a multi-component type
 * key for Map.
 */
public final class ClassIndex implements AutoCloseable {
    public final static int INT_BYTES_SHIFT = 2;
    public static final int DEFAULT_HASH_BIT = 20; // 1MB -> about 1K classes
    public static final int MIN_HASH_BIT = 14;
    public static final int MAX_HASH_BIT = 24;
    private static final System.Logger LOGGER = Logging.getLogger();
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final IndexedObjectCache cache = new IndexedObjectCache();
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
        this(DEFAULT_HASH_BIT, true, Logging.Context.TEST);
    }

    public ClassIndex(int hashBit, boolean fallbackMapEnabled, Logging.Context loggingContext) {
        this.hashBit = Math.min(Math.max(hashBit, MIN_HASH_BIT), MAX_HASH_BIT);
        this.fallbackMapEnabled = fallbackMapEnabled;
        capacity = (1 << hashBit) << INT_BYTES_SHIFT;
        memoryAddress = unsafe.allocateMemory(capacity);
        unsafe.setMemory(memoryAddress, capacity, (byte) 0);
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
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
        if(currentIndex == 0) {
            int idx = fallbackMapEnabled ? fallbackMap.get((Class<?>) newClass) : atomicIndex.incrementAndGet();
            // use cas to check, is there an index to an existing class
            if(unsafe.compareAndSwapInt(null, i, 0, idx)) {
                cache.set(idx - 1, newClass);
                return idx;
            } else {
                // whether the existing index and the new index are the same. If they are different, it means there is a hash conflict.
                if(idx != unsafe.getIntVolatile(null, i)) {
                    useFallbackMap.set(true);
                }
                return idx;
            }
        } else {
            if (cache.getVolatile(currentIndex - 1) != newClass) {
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
        int index = unsafe.getInt(getIdentityAddress(identityHashCode, memoryAddress));
        if (index != 0 && cache.get(index - 1) == klass) {
            return index;
        }
        return 0;
    }

    public int getObjectIndexVolatile(Object klass) {
        if (useFallbackMap.get()) {
            return fallbackMap.get((Class<?>) klass);
        }
        int identityHashCode = capHashCode(System.identityHashCode(klass), hashBit);
        int index = unsafe.getIntVolatile(null, getIdentityAddress(identityHashCode, memoryAddress));
        if (index != 0 && cache.get(index - 1) == klass) {
            return index;
        }
        return 0;
    }

    public int getIndexOrAddClass(Class<?> klass) {
        return getIndexOrAddObject(klass);
    }

    public int getIndexOrAddObject(Object klass) {
        int value =  getObjectIndex(klass); // use getObjectIndex instead of getObjectIndexVolatile
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

    /**
     * Provides a multi-component type key by implementing the counting-sort algorithm
     *
     * @param objects the given components
     * @return the multi-component type key
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public IndexKey getIndexKey(Object[] objects) {
        int length = objects.length;
        boolean[] checkArray = new boolean[index + length + 1];
        int min = Integer.MAX_VALUE, max = 0;
        for (int i = 0; i < length; i++) {
            int value = getIndexOrAddClass(objects[i].getClass());
            if (checkArray[value]) {
                throw new IllegalArgumentException("Duplicate object types are not allowed");
            }
            checkArray[value] = true;
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        return new IndexKey(checkArray, min, max, length);
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public IndexKey getIndexKeyByType(Class<?>[] classes) {
        int length = classes.length;
        boolean[] checkArray = new boolean[index + length + 1];
        int min = Integer.MAX_VALUE, max = 0;
        for (int i = 0; i < length; i++) {
            int value = getIndexOrAddClass(classes[i]);
            if (checkArray[value]) {
                throw new IllegalArgumentException("Duplicate object types are not allowed");
            }
            checkArray[value] = true;
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        return new IndexKey(checkArray, min, max, length);
    }

    public <E extends Enum<E>> IndexKey getIndexKeyByEnum(E enumValue) {
        int cIndex = getIndex(enumValue.getClass());
        cIndex = cIndex == 0 ? getIndexOrAddClass(enumValue.getClass()) : cIndex;
        return new IndexKey(new int[]{cIndex, enumValue.ordinal()});
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