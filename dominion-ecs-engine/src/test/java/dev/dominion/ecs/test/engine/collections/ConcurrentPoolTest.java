package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentPoolTest {

    @Test
    void newTenant() {
        Assertions.assertNotNull(new ConcurrentPool<Object[]>().newTenant());
    }

    @Nested
    public class TenantTest {

        @Test
        public void nextId() {
            try (ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant()) {
                Assertions.assertEquals(0, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(0, (tenant.nextId() >> ConcurrentPool.PAGE_CAPACITY_BIT_SIZE)
                        & ConcurrentPool.PAGE_INDEX_BIT_MASK);
                for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY; i++) {
                    tenant.nextId();
                }
                Assertions.assertEquals(1, (tenant.nextId() >> ConcurrentPool.PAGE_CAPACITY_BIT_SIZE)
                        & ConcurrentPool.PAGE_INDEX_BIT_MASK);
                Assertions.assertEquals(4, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
            }
        }

        @Test
        public void freeId() {
            try (ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant()) {
                Assertions.assertEquals(0, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                tenant.freeId(1);
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(2, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                tenant.freeId(0);
                Assertions.assertEquals(0, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                tenant.freeId(1);
                tenant.freeId(2);
                Assertions.assertEquals(2, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(3, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
            }
        }

        @Test
        public void concurrentIds() throws InterruptedException {
            try (ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant()) {
                final int capacity = 1 << 22;
                final ExecutorService pool = Executors.newFixedThreadPool(4);
                for (int i = 0; i < capacity; i++) {
                    if (i % 10 == 0) {
                        final int idx = i - 1;
                        pool.execute(() -> tenant.freeId(idx));
                    }
                    pool.execute(tenant::nextId);
                }
                pool.shutdown();
                Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
                long finalId = tenant.nextId();
                Assertions.assertEquals((int) (capacity * .9), finalId);
            }
        }
    }

    @Nested
    public class PageTest {

        @Test
        public void size() {
            ConcurrentPool.Page<Object> page = new ConcurrentPool.Page<>(0, null);
            Assertions.assertEquals(0, page.getAndIncrementSize());
            Assertions.assertEquals(1, page.getAndIncrementSize());
            Assertions.assertEquals(1, page.decrementSize());
        }

        @Test
        public void capacity() {
            ConcurrentPool.Page<Object> page = new ConcurrentPool.Page<>(0, null);
            Assertions.assertTrue(page.hasCapacity());
            for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY - 1; i++) {
                page.getAndIncrementSize();
            }
            Assertions.assertTrue(page.hasCapacity());
            page.getAndIncrementSize();
            Assertions.assertFalse(page.hasCapacity());
            page.decrementSize();
            Assertions.assertTrue(page.hasCapacity());
        }

        @Test
        public void data() {
            ConcurrentPool.Page<Integer> previous = new ConcurrentPool.Page<>(0, null);
            ConcurrentPool.Page<Integer> page = new ConcurrentPool.Page<>(0, previous);
            Integer value = 1;
            page.set(10, value);
            Assertions.assertEquals(value, page.get(10));
            Assertions.assertEquals(previous, page.getPrevious());
        }
    }
}
