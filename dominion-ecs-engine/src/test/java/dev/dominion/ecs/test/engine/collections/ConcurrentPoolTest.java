package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.SparseIntMap;
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
        public void concurrentNextId() throws InterruptedException {
            final int capacity = 1 << 22;
            ConcurrentPool.Tenant<Object[]> tenant = new ConcurrentPool<Object[]>().newTenant();
            final ExecutorService pool = Executors.newFixedThreadPool(8);
            for (int i = 0; i < capacity; i++) {
                pool.execute(tenant::nextId);
            }
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            long finalId = tenant.nextId();
            Assertions.assertEquals(capacity, finalId);
        }

    }
}
