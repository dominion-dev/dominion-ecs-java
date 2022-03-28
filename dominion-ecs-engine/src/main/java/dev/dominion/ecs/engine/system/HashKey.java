/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.util.Arrays;

/**
 * HashKey objects run in Dominion's critical path, better performance here is not just an option.
 * In case of an int hashCode collision, the equals() method will compare the long value of the HashKey and the
 * most significant byte of each data element to keep the event of a HashKey collision statistically irrelevant.
 */
public final class HashKey {
    private final long value;
    private final byte[] data;

    public HashKey(int value) {
        this.value = value;
        data = null;
    }

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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(((HashKey) o).data, data) && ((HashKey) o).value == value ;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    @Override
    public String toString() {
        return "|" + value + ":"
                + Arrays.toString(data) + "|";
    }
}
