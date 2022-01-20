package dev.dominion.ecs.engine.system;

import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public final class ClassIndex implements AutoCloseable {
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final static int INT_BYTES_SHIFT = 2;
    private static final int DEFAULT_HASH_BITS = 14;
    private static final int MAX_HASH_BITS = 24;
    private final StampedLock lock = new StampedLock();
    private final Map<Integer, Class<?>> controlData = new HashMap<>(1 << 10);
    private long address;
    private int hashBits;
    private int capacity;
    private int index = 0;

    public ClassIndex() {
        this(DEFAULT_HASH_BITS);
    }

    public ClassIndex(int hashBits) {
        if (hashBits < 1 || hashBits > MAX_HASH_BITS)
            throw new IllegalArgumentException("Hash cannot be less than 1 or greater than " + MAX_HASH_BITS + " bits");
        this.hashBits = hashBits;
        address = ensureCapacity(0, 1 << hashBits);
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
        long stamp = lock.writeLock();
        int index;
        try {
            index = add(newClass);
        } finally {
            lock.unlockWrite(stamp);
        }
        return index;
    }

    private int add(Class<?> newClass) {
        final int identityHashCode = getIdentityHashCode(newClass, hashBits);
        final long i = getIdentityAddress(identityHashCode);
        int currentIndex = unsafe.getInt(i);
        if (currentIndex == 0) {
            unsafe.putInt(i, ++index);
            controlData.put(index, newClass);
            return index;
        } else {
            final Class<?> currentClass = controlData.get(currentIndex);
            assert currentClass != null : currentIndex + " cannot be null in ClassIndex.controlData map";
            if (currentClass != newClass) {
                if (hashBits < MAX_HASH_BITS) {
                    reindexAll(++hashBits);
                    return add(newClass);
                } else {
                    throw new RuntimeException(
                            "An hash(" + hashBits + "bits) collision has been detected between "
                                    + newClass.getName() + "-" + getIdentityHashCode(newClass, hashBits) + " class and previous "
                                    + currentClass.getName() + "-" + getIdentityHashCode(currentClass, hashBits) + " class");
                }
            }
        }
        return currentIndex;
    }

    private void reindexAll(int hashBits) {
        address = ensureCapacity(address, 1 << hashBits);
        for (Map.Entry<Integer, Class<?>> entry : controlData.entrySet()) {
            final int newIdentityHashCode = getIdentityHashCode(entry.getValue(), hashBits);
            unsafe.putInt(getIdentityAddress(newIdentityHashCode), entry.getKey());
        }
    }

    public int getIndex(Class<?> klass) {
        final int identityHashCode = getIdentityHashCode(klass, hashBits);
        return unsafe.getInt(getIdentityAddress(identityHashCode));
    }

    public int getIndexOrAddClass(Class<?> klass) {
        int value;
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L)
                    continue;
                // possibly racy reads
                value = getIndex(klass);
                if (!lock.validate(stamp))
                    continue;
                if (value != 0)
                    break;
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L)
                    continue;
                // exclusive access
                value = add(klass);
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

    private int getIdentityHashCode(Class<?> klass, int hashBits) {
        return System.identityHashCode(klass) >> (32 - hashBits);
    }

    private long getIdentityAddress(long identityHashCode) {
        return address + (identityHashCode << INT_BYTES_SHIFT);
    }

    public int getHashBits() {
        return hashBits;
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
            address = ensureCapacity(address, capacity);
            index = 0;
            controlData.clear();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void close() {
        unsafe.freeMemory(address);
        controlData.clear();
    }
}
