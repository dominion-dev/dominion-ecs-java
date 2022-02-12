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
    public static final int DEFAULT_HASH_BITS = 20;
    public static final int MIN_HASH_BITS = 14;
    public static final int MAX_HASH_BITS = 24;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final Map<Object, Integer> fallbackMap = new ConcurrentHashMap<>(1 << 10);
    private final AtomicBoolean useFallbackMap = new AtomicBoolean(false);
    private final AtomicInteger index = new AtomicInteger(0);
    private final int hashBits;
    private long memoryAddress;
    private int capacity;

    public ClassIndex() {
        this(DEFAULT_HASH_BITS);
    }

    public ClassIndex(int hashBits) {
        if (hashBits < MIN_HASH_BITS || hashBits > MAX_HASH_BITS)
            throw new IllegalArgumentException("Hash cannot be less than " + MIN_HASH_BITS + " or greater than " + MAX_HASH_BITS + " bits");
        this.hashBits = hashBits;
        memoryAddress = ensureCapacity(0, 1 << hashBits);
    }

    private static long getIdentityAddress(long identityHashCode, long address) {
        return address + (identityHashCode << INT_BYTES_SHIFT);
    }

    private long ensureCapacity(long address, int newCapacity) {
        long newAddress = address;
        if (newCapacity != capacity) {
            newAddress = unsafe.reallocateMemory(address, (long) newCapacity << INT_BYTES_SHIFT);
        }
        unsafe.setMemory(newAddress, (long) newCapacity << INT_BYTES_SHIFT, (byte) 0);
        capacity = newCapacity;
        return newAddress;
    }

    public int addClass(Class<?> newClass) {
        return addObject(newClass);
    }

    public int addObject(Object newClass) {
        if (useFallbackMap.get()) {
            return fallbackMap.computeIfAbsent(newClass, k -> index.incrementAndGet());
        }
        final int identityHashCode = capHashCode(System.identityHashCode(newClass), hashBits);
        final long i = getIdentityAddress(identityHashCode, memoryAddress);
        int currentIndex = unsafe.getInt(i);
        if (currentIndex == 0) {
            int idx = index.incrementAndGet();
            unsafe.putInt(i, idx);
            fallbackMap.put(newClass, idx);
            return idx;
        } else {
            if (!fallbackMap.containsKey(newClass)) {
                useFallbackMap.set(true);
//                System.out.println("USE FALLBACK MAP");
                int idx = index.incrementAndGet();
                fallbackMap.put(newClass, idx);
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
            return fallbackMap.get(klass);
        }
        int identityHashCode = capHashCode(System.identityHashCode(klass), hashBits);
        return unsafe.getInt(getIdentityAddress(identityHashCode, memoryAddress));
    }

    public int getIndexOrAddClass(Class<?> klass) {
        return getIndexOrAddObject(klass);
    }

    public int getIndexOrAddObject(Object klass) {
        int value = getObjectIndex(klass);
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
        boolean[] checkArray = new boolean[index.get() + objects.length + 1];
        int min = capacity, max = 0;
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

    public int getHashBits() {
        return hashBits;
    }

    public int getCapacity() {
        return capacity;
    }

    public int size() {
        return index.get();
    }

    public boolean isEmpty() {
        return index.get() == 0;
    }

    public void useUseFallbackMap() {
        useFallbackMap.set(true);
    }

    public void clear() {
        memoryAddress = ensureCapacity(memoryAddress, capacity);
        index.set(0);
        fallbackMap.clear();
    }

    @Override
    public void close() {
        fallbackMap.clear();
        unsafe.freeMemory(memoryAddress);
    }
}
