/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public final class ClassIndex implements AutoCloseable {
    public final static int INT_BYTES_SHIFT = 2;
    public static final int DEFAULT_HASH_BITS = 14;
    public static final int MAX_HASH_BITS = 24;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final StampedLock lock = new StampedLock();
    private final Map<Integer, Integer> controlData = new HashMap<>(1 << 10);
    private Memory memory;
    private int capacity;
    private int index = 0;


    public ClassIndex() {
        this(DEFAULT_HASH_BITS);
    }

    public ClassIndex(int hashBits) {
        if (hashBits < 1 || hashBits > MAX_HASH_BITS)
            throw new IllegalArgumentException("Hash cannot be less than 1 or greater than " + MAX_HASH_BITS + " bits");
        memory = new Memory(ensureCapacity(0, 1 << hashBits), hashBits);
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
        return addClassByHashCode(newClass, System.identityHashCode(newClass));
    }

    public int addClassByHashCode(Class<?> newClass, int hashCode) {
        long stamp = lock.writeLock();
        int index;
        try {
            index = add(newClass, hashCode);
        } finally {
            lock.unlockWrite(stamp);
        }
        return index;
    }

    private int add(Class<?> newClass, int hashCode) {
        final int identityHashCode = capHashCode(hashCode, memory.hashBits);
        final long i = getIdentityAddress(identityHashCode, memory.address);
        int currentIndex = unsafe.getInt(i);
        if (currentIndex == 0) {
            unsafe.putInt(i, ++index);
            controlData.put(index, hashCode);
            return index;
        } else {
            int currentHashCode = controlData.get(currentIndex);
            if (currentHashCode != hashCode) {
                if (memory.hashBits < MAX_HASH_BITS) {
                    reindexAll();
                    return add(newClass, hashCode);
                } else {
                    throw new RuntimeException(
                            "An hash(" + memory.hashBits + "bits) collision has been detected");
                }
            }
        }
        return currentIndex;
    }

    private void reindexAll() {
        int hashBits = memory.hashBits + 1;
        Memory newMemory = new Memory(ensureCapacity(memory.address, 1 << hashBits), hashBits);
        for (Map.Entry<Integer, Integer> entry : controlData.entrySet()) {
            final int newIdentityHashCode = capHashCode(entry.getValue(), hashBits);
            unsafe.putInt(getIdentityAddress(newIdentityHashCode, newMemory.address), entry.getKey());
        }
        memory = newMemory;
    }

    public int getIndex(Class<?> klass) {
        return getIndexByHashCode(System.identityHashCode(klass));
    }

    public int getIndexByHashCode(int hashCode) {
        long stamp = lock.tryOptimisticRead();
        for (; ; ) {
            int identityHashCode = capHashCode(hashCode, memory.hashBits);
            int index = unsafe.getInt(getIdentityAddress(identityHashCode, memory.address));
            if (!lock.validate(stamp)) {
                stamp = lock.tryOptimisticRead();
                continue;
            }
            return index;
        }
    }

    public int getIndexOrAddClass(Class<?> klass) {
        return getIndexOrAddClassByHashCode(klass, System.identityHashCode(klass));
    }

    public int getIndexOrAddClassByHashCode(Class<?> klass, int hashCode) {
        int value;
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L)
                    continue;
                // possibly racy reads
                value = getIndexByHashCode(hashCode);
                if (!lock.validate(stamp))
                    continue;
                if (value != 0)
                    break;
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L)
                    continue;
                // exclusive access
                value = add(klass, hashCode);
                break;
            }
            return value;
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
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
        return memory.hashBits;
    }

    public int getCapacity() {
        return capacity;
    }

    public int size() {
        return index;
    }

    public boolean isEmpty() {
        return index == 0;
    }

    public void clear() {
        long stamp = lock.writeLock();
        try {
            memory = new Memory(ensureCapacity(memory.address, capacity), memory.hashBits);
            index = 0;
            controlData.clear();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void close() {
        unsafe.freeMemory(memory.address);
        controlData.clear();
    }

    record Memory(long address, int hashBits) {
    }
}
