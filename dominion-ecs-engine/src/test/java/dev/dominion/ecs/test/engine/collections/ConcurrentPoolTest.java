package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConcurrentPoolTest {

    @Test
    void newTenant() {
        Assertions.assertNotNull(new ConcurrentPool<Object[]>().newTenant());
    }

    @Nested
    public class TenantTest {

        @Test
        public void nextIdTest() {
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
    }
}
