package dev.dominion.ecs.test.engine.system;

import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.Logging;
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
    void size() {
        try (ClassIndex map = new ClassIndex()) {
            map.addClass(C1.class);
            map.addClass(C2.class);
            map.addClass(C3.class);
            Assertions.assertEquals(3, map.size());
        }
    }

    @Test
    void concurrentGetIndexOrAddClass() throws InterruptedException {
        try (ClassIndex map = new ClassIndex(22, false, Logging.Context.TEST)) {
            final int capacity = 1 << 7;
            final ExecutorService executorService = Executors.newFixedThreadPool(4);
            AtomicInteger errors = new AtomicInteger(0);
            for (int i = 0; i < capacity; i++) {
                executorService.execute(() -> {
                    Object newClass = new Object();
                    var wIndex = map.addObject(newClass);
                    int rIndex;
                    if ((rIndex = map.getObjectIndex(newClass)) != wIndex) {
                        System.out.println("rIndex = " + rIndex);
                        System.out.println("wIndex = " + wIndex);
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
