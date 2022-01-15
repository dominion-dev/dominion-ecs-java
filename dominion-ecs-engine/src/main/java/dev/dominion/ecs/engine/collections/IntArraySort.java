package dev.dominion.ecs.engine.collections;

public final class IntArraySort {

    public static final int DEFAULT_CAPACITY = 1 << 8;

    public static int[] sort(int[] input) {
        return sort(input, DEFAULT_CAPACITY);
    }

    public static int[] sort(int[] input, int capacity) {
        return sort(input, 0, input.length, capacity);
    }

    public static int[] sort(int[] input, int start, int end, int capacity) {
        boolean[] checkArray = new boolean[capacity];
        int[] sorted = new int[end - start];
        int min = capacity, max = 0;
        for (int i = start; i < end; i++) {
            int value = input[i];
            if (checkArray[value]) {
                throw new DuplicateValueException(value);
            }
            checkArray[value] = true;
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        int j = 0;
        for (int i = min; i <= max; i++) {
            if (checkArray[i]) {
                sorted[j++] = i;
            }
        }
        return sorted;
    }

    public static class DuplicateValueException extends IllegalArgumentException {
        public DuplicateValueException(int duplicateValue) {
            super("Duplicate values are not allowed: " + duplicateValue);
        }
    }
}
