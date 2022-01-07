package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.IntArraySort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntArraySortTest {

    @Test
    void sort() {
        Assertions.assertArrayEquals(
                new int[]{10, 20, 30, 70, 100, 120, 150},
                IntArraySort.sort(new int[]{70, 20, 100, 150, 30, 10, 120}));
        Assertions.assertArrayEquals(
                new int[]{30, 100, 150},
                IntArraySort.sort(new int[]{70, 20, 100, 150, 30, 10, 120}, 2, 5, 1024));
        Assertions.assertThrows(
                IntArraySort.DuplicateValueException.class,
                () -> IntArraySort.sort(new int[]{70, 20, 100, 150, 30, 20, 120}));
    }
}
