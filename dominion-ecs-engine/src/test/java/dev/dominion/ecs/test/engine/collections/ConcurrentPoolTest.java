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
    public void newTenant() {
        try (ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant()) {
            Assertions.assertNotNull(tenant);
        }
    }

    @Test
    public void register() {
        ConcurrentPool<Object[]> concurrentPool = new ConcurrentPool<>();
        try (ConcurrentPool.Tenant<Object[]> tenant = concurrentPool.newTenant()) {
            Object[] entry = new Object[0];
            Assertions.assertEquals(entry, tenant.register(1, entry));
            Assertions.assertEquals(entry, concurrentPool.getEntry(1));
        }
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
                Assertions.assertEquals(1, tenant.currentPageSize());
                Assertions.assertEquals(0, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(2, tenant.currentPageSize());
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(3, tenant.currentPageSize()); // ready nextId == 2
                tenant.freeId(0); // 1 -> 0 : ready nextId == 1
                Assertions.assertEquals(2, tenant.currentPageSize());
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(3, tenant.currentPageSize());
                Assertions.assertEquals(2, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                // move to the next page
                for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY; i++) {
                    tenant.nextId();
                }
                Assertions.assertEquals(4, tenant.currentPageSize());
                Assertions.assertEquals(3, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                tenant.freeId(1);
                Assertions.assertEquals(5, tenant.currentPageSize());
                Assertions.assertEquals(65535, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(4, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(6, tenant.currentPageSize());
            }
        }

        @Test
        public void concurrentIds() throws InterruptedException {
            ConcurrentPool<Object[]> concurrentPool = new ConcurrentPool<>();
            try (ConcurrentPool.Tenant<Object[]> tenant = concurrentPool.newTenant()) {
                final int capacity = 1 << 22;
                final ExecutorService pool = Executors.newFixedThreadPool(4);
                int removed = 0;
                for (int i = 0; i < capacity; i++) {
                    if (i % 10 == 0) {
                        final int idx = (int) (i * 0.7);
                        pool.execute(() -> tenant.freeId(idx));
                        removed++;
                    }
                    pool.execute(tenant::nextId);
                }
                pool.shutdown();
                Assertions.assertTrue(pool.awaitTermination(600, TimeUnit.SECONDS));
                Assertions.assertEquals(capacity + 1 - removed, concurrentPool.size());
                Assertions.assertEquals((int) (capacity * .9), tenant.nextId());
            }
        }
    }

    @Nested
    public class LinkedPageTest {

        @Test
        public void size() {
            ConcurrentPool.LinkedPage<Object> page = new ConcurrentPool.LinkedPage<>(0, null);
            Assertions.assertEquals(0, page.incrementIndex());
            Assertions.assertEquals(1, page.incrementIndex());
            Assertions.assertEquals(0, page.decrementIndex());
        }

        @Test
        public void capacity() {
            ConcurrentPool.LinkedPage<Object> page = new ConcurrentPool.LinkedPage<>(0, null);
            Assertions.assertTrue(page.hasCapacity());
            for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY - 1; i++) {
                page.incrementIndex();
            }
            Assertions.assertTrue(page.hasCapacity());
            page.incrementIndex();
            Assertions.assertFalse(page.hasCapacity());
            page.decrementIndex();
            Assertions.assertTrue(page.hasCapacity());
        }

        @Test
        public void data() {
            ConcurrentPool.LinkedPage<Integer> previous = new ConcurrentPool.LinkedPage<>(0, null);
            ConcurrentPool.LinkedPage<Integer> page = new ConcurrentPool.LinkedPage<>(0, previous);
            Integer value = 1;
            page.set(10, value);
            Assertions.assertEquals(value, page.get(10));
            Assertions.assertEquals(previous, page.getPrevious());
        }
    }
}
