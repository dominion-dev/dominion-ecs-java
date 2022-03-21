/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

public final class HashKey {
    private final long value;
    private final int length;
    private final int last;

    public HashKey(int value) {
        this.value = value;
        length = 1;
        last = value;
    }

    public HashKey(int[] array) {
        int result = 1;
        for (int value : array) {
            result = result * 31 + value;
        }
        value = result;
        length = array.length;
        last = array[length - 1];
    }

    public HashKey(boolean[] checkArray, int min, int max) {
        int result = 1;
        int idx = 0;
        int i = min;
        for (; i <= max; i++) {
            if (checkArray[i]) {
                result = result * 31 + i;
                idx++;
            }
        }
        value = result;
        length = idx;
        last = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return ((HashKey) o).length == length && ((HashKey) o).value == value && ((HashKey) o).last == last;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }
}
