/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.util.Arrays;

/**
 * IndexKey objects run in Dominion's critical path, better performance here is not just an option.
 * In case of an int hashCode collision, the equals() method will compare the long value of the IndexKey and the
 * two most significant bytes (after the xor operation) of each data element to keep the event of a IndexKey collision
 * statistically irrelevant.
 */
public final class IndexKey {
    private final int hashCode;
    private final byte[] data;

    public IndexKey(int value) {
        this.hashCode = value;
        data = null;
    }

    public IndexKey(int[] array) {
        long result = 1;
        int length = array.length;
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            int value = array[i];
            result = result * 31 + value;
            data[i] = (byte) (value ^ (value >>> 8));
        }
        hashCode = (int) (result ^ (result >>> 32));
    }

    public IndexKey(boolean[] checkArray, int min, int max, int length) {
        long result = 1;
        int idx = 0;
        int i = min;
        data = new byte[length];
        for (; i <= max; i++) {
            if (checkArray[i]) {
                result = result * 31 + i;
                data[idx++] = (byte) (i ^ (i >>> 8));
            }
        }
        hashCode = (int) (result ^ (result >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(((IndexKey) o).data, data);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "|" + hashCode + ":"
                + Arrays.toString(data) + "|";
    }
}
