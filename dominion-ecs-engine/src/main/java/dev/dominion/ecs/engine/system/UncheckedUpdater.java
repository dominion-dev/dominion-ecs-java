/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UncheckedUpdater<T> {
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    protected final long offset;

    protected UncheckedUpdater(Class<T> tClass, String fieldName) throws NoSuchFieldException {
        Field field = tClass.getDeclaredField(fieldName);
        offset = unsafe.objectFieldOffset(field);
    }

    public static final class Reference<T, V> extends UncheckedUpdater<T> {
        public Reference(Class<T> tClass, String fieldName) throws NoSuchFieldException {
            super(tClass, fieldName);
        }

        public boolean compareAndSet(T obj, V expect, V update) {
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }
    }

    public static final class Int<T> extends UncheckedUpdater<T> {
        public Int(Class<T> tClass, String fieldName) throws NoSuchFieldException {
            super(tClass, fieldName);
        }

        public boolean compareAndSet(T obj, int expect, int update) {
            return unsafe.compareAndSwapInt(obj, offset, expect, update);
        }
    }
}
