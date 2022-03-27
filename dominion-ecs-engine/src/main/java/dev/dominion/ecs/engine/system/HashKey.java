/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.util.Arrays;

/**
 * HashKey objects run in Dominion's critical path, better performance here is not just an option.
 * The constructor with int[] assumes to receive a sorted array as parameter.
 * The constructor with boolean[] always produces a counting-sorted int[].
 * <p>
 * In case of hashCode collision the equals() will compare the most significant byte of each data element.
 * In the "equals" algorithm, because the HashKey data elements are sorted, the only critical byte collision you might
 * have due to int down-casting can only occur in the last data element. For this reason, the "equals" method will check
 * the "last" property to avoid any HashKey collision.
 */
public final class HashKey {
    private final long value;
    private final int last;
    private final byte[] data;

    public HashKey(int value) {
        this.value = value;
        last = value;
        data = null;
    }

    /**
     * @param array must be a sorted array.
     */
    public HashKey(int[] array) {
        int result = 1;
        int length = array.length;
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            int value = array[i];
            result = result * 31 + value;
            data[i] = (byte) value;
        }
        value = result;
        last = array[length - 1];
    }

    public HashKey(boolean[] checkArray, int min, int max) {
        int result = 1;
        int idx = 0;
        int i = min;
        data = new byte[((max - min) + 1)];
        for (; i <= max; i++) {
            if (checkArray[i]) {
                result = result * 31 + i;
                data[idx++] = (byte) i;
            }
        }
        value = result;
        last = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return ((HashKey) o).last == last && Arrays.equals(((HashKey) o).data, data);
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    @Override
    public String toString() {
        return "|" + value + ":"
                + last + ":"
                + Arrays.toString(data) + "|";
    }
}
