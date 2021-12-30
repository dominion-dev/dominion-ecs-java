package dev.dominion.ecs.engine.system;

import java.util.Arrays;

public final class HashCode {

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static long longHashCode(int[] input) {
        long hashCode = 0;
        for (int i = 0; i < input.length; i++) {
            hashCode = 31 * hashCode + input[i];
        }
        return hashCode;
    }

    public static long sortedInputHashCode(int[] input) {
        Arrays.sort(input);
        return longHashCode(input);
    }
}
