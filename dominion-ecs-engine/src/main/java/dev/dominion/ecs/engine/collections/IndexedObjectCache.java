/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */
package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

/**
 * @author endison1986
 */
public class IndexedObjectCache {
    private static final Unsafe U = UnsafeFactory.INSTANCE;
    private static final int MAX_CAPACITY = 1 << 30;
    private static final long CAPCTL;
    private static final int ABASE;
    private static final int ASHIFT;
    private volatile Object[] values;
    private volatile int capacity;

    static {
        try {
            CAPCTL = U.objectFieldOffset(IndexedObjectCache.class.getDeclaredField("capacity"));
            ABASE = U.arrayBaseOffset(Object[].class);
            int scale = U.arrayIndexScale(Object[].class);
            if ((scale & (scale - 1)) != 0) {
                throw new Error("array index scale not a power of two");
            }
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public Object get(int index) {
        return U.getObject(values, offset(ABASE, ASHIFT, index));
    }

    public Object getVolatile(int index) {
        return U.getObjectVolatile(values, offset(ABASE, ASHIFT, index));
    }

    public void set(int index, Object value) {
        ensureCapacity(index + 1);
        final long offset = offset(ABASE, ASHIFT, index);
        for (; ; ) {// like cas
            final Object[] before = values;
            U.putOrderedObject(before, offset, value);
            final Object[] after = values;

            if (before == after) {
                return;
            }
        }
    }

    private void ensureCapacity(int minCapacity) {
        int cap;
        while (minCapacity > (cap = capacity)) {
            if (cap < 0) {
                Thread.yield();
            } else if (U.compareAndSwapInt(this, CAPCTL, cap, -1)) {
                Object[] finalArray = values;

                int newCapacity = tableSizeFor(minCapacity);
                if (newCapacity > MAX_CAPACITY) {
                    throw new IndexOutOfBoundsException("" + newCapacity);
                }

                Object[] objs = new Object[newCapacity];

                if (finalArray != null) {
                    System.arraycopy(finalArray, 0, objs, 0, finalArray.length);
                }
                values = objs;
                capacity = newCapacity;
            }
        }
    }
    public int tableSizeFor(final int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAX_CAPACITY) ? MAX_CAPACITY : n + 1;
    }
    public long offset(final long arrayBase, final int arrayShift, final int index) {
        return ((long) index << arrayShift) + arrayBase;
    }
}