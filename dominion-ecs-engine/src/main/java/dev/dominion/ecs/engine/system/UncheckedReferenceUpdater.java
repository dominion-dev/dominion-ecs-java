/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class UncheckedReferenceUpdater<T, V> {
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final long offset;

    public UncheckedReferenceUpdater(Class<T> tClass, Class<V> vClass, String fieldName) throws NoSuchFieldException {
        Field field = tClass.getDeclaredField(fieldName);
        offset = unsafe.objectFieldOffset(field);
    }

    public boolean compareAndSet(T obj, V expect, V update) {
        return unsafe.compareAndSwapObject(obj, offset, expect, update);
    }
}
