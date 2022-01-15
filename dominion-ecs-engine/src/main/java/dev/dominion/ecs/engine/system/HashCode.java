package dev.dominion.ecs.engine.system;

public final class HashCode {

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static long longHashCode(int[] input) {
        long hashCode = 0;
        for (int i = 0; i < input.length; i++) {
            hashCode = 31 * hashCode + input[i];
        }
        return hashCode;
    }
}
