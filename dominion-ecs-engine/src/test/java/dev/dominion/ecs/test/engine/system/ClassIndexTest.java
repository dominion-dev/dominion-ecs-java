package dev.dominion.ecs.test.engine.system;

import dev.dominion.ecs.engine.system.ClassIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassIndexTest {

    @Test
    void addClass() {
        try (ClassIndex map = new ClassIndex()) {
            Assertions.assertEquals(1, map.addClass(C1.class));
            Assertions.assertEquals(2, map.addClass(C2.class));
            Assertions.assertEquals(2, map.addClass(C2.class));
        }
    }

    @Test
    void getIndex() {
        try (ClassIndex map = new ClassIndex()) {
            map.addClass(C1.class);
            Assertions.assertEquals(0, map.getIndex(C2.class));
            map.addClass(C2.class);
            map.addClass(C1.class);
            Assertions.assertEquals(1, map.getIndex(C1.class));
            Assertions.assertEquals(2, map.getIndex(C2.class));
        }
    }

    @Test
    void reindex() {
        try (ClassIndex map = new ClassIndex(1)) {
            map.addClass(C1.class);
            map.addClass(C1.class);
            Assertions.assertEquals(1, map.getHashBits());
            Assertions.assertEquals(1, map.getIndex(C1.class));
            map.addClass(C2.class);
            Assertions.assertTrue(map.getHashBits() > 1);
            Assertions.assertEquals(1, map.getIndex(C1.class));
            Assertions.assertEquals(2, map.getIndex(C2.class));
            map.addClass(C3.class);
            Assertions.assertTrue(map.getHashBits() > 2);
            Assertions.assertEquals(1, map.getIndex(C1.class));
            Assertions.assertEquals(2, map.getIndex(C2.class));
            Assertions.assertEquals(3, map.getIndex(C3.class));
        }
    }

    @Test
    void sizeAndCapacity() {
        try (ClassIndex map = new ClassIndex(1)) {
            Assertions.assertEquals(2, map.getCapacity());
            map.addClass(C1.class);
            map.addClass(C2.class);
            map.addClass(C3.class);
            Assertions.assertEquals(3, map.size());
            Assertions.assertTrue(map.getCapacity() > 2);
            map.clear();
            Assertions.assertTrue(map.isEmpty());
            Assertions.assertTrue(map.getCapacity() > 2);
        }
    }

    private static class C1 {
    }

    private static class C2 {
    }

    private static class C3 {
    }
}
