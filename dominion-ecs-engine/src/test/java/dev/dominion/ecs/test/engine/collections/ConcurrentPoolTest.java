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
            ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant();
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

        @Test
        public void freeId() {
            ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant();
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

        @Test
        public void concurrentIds() throws InterruptedException {
            final int capacity = 1 << 22;
            ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant();
            final ExecutorService pool = Executors.newFixedThreadPool(2);
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
