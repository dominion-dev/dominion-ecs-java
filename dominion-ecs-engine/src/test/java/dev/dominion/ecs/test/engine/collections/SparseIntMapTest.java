package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SparseIntMapTest {

    @Test
    void unmodifiable() {
        SparseIntMap<Integer> unmodifiable = SparseIntMap.UnmodifiableView.wrap(new ConcurrentIntMap<>());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> unmodifiable.put(0, 0));
        Assertions.assertTrue(unmodifiable.isEmpty());
    }
}
