package dev.dominion.ecs.test.engine.system;

import dev.dominion.ecs.engine.system.ClassIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    void getIndexOrAddClass() {
        try (ClassIndex map = new ClassIndex()) {
            Assertions.assertEquals(1, map.getIndexOrAddClass(C1.class));
            Assertions.assertEquals(2, map.getIndexOrAddClass(C2.class));
            Assertions.assertEquals(1, map.getIndexOrAddClass(C1.class));
            Assertions.assertEquals(2, map.getIndexOrAddClass(C2.class));
        }
    }

    @Test
    void getIndexOrAddClassBatch() {
        try (ClassIndex map = new ClassIndex()) {
            Assertions.assertArrayEquals(new int[]{1, 2, 3}
                    , map.getIndexOrAddClassBatch(new Class<?>[]{C1.class, C2.class, C3.class}));
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

    @Test
    void concurrentGetIndexOrAddClass() throws InterruptedException {
        try (ClassIndex map = new ClassIndex(1)) {
            final int capacity = 1 << 12;
            final ExecutorService executorService = Executors.newFixedThreadPool(8);
            AtomicInteger errors = new AtomicInteger(0);
            for (int i = 0; i < capacity; i++) {
                executorService.execute(() -> {
                    int hashCode = (int) (Math.random() * (1 << 30));
                    var index = map.addClassByHashCode(null, hashCode);
                    if (map.getIndexByHashCode(hashCode) != index) {
                        errors.incrementAndGet();
                    }
                });
            }
            executorService.shutdown();
            Assertions.assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
            Assertions.assertEquals(capacity, map.size());
            Assertions.assertEquals(0, errors.get());
        }
    }

    private static class C1 {
    }

    private static class C2 {
    }

    private static class C3 {
    }
}
