package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentPoolTest {

    @Test
    public void newTenant() {
        try (ConcurrentPool.Tenant<LongEntity> tenant = new ConcurrentPool<LongEntity>().newTenant()) {
            Assertions.assertNotNull(tenant);
        }
    }

    @Test
    public void register() {
        ConcurrentPool<LongEntity> concurrentPool = new ConcurrentPool<>();
        try (ConcurrentPool.Tenant<LongEntity> tenant = concurrentPool.newTenant()) {
            LongEntity entry = new LongEntity(1, null);
            Assertions.assertEquals(entry, tenant.register(1, entry));
            Assertions.assertEquals(entry, concurrentPool.getEntry(1));
        }
    }

    @Nested
    public class TenantTest {

        @Test
        public void nextId() {
            try (ConcurrentPool.Tenant<LongEntity> tenant = new ConcurrentPool<LongEntity>().newTenant()) {
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
            try (ConcurrentPool.Tenant<LongEntity> tenant = new ConcurrentPool<LongEntity>().newTenant()) {
                Assertions.assertEquals(0, tenant.currentPageSize());
                Assertions.assertEquals(0, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(1, tenant.currentPageSize());
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(2, tenant.currentPageSize()); // ready nextId == 2
                tenant.freeId(0); // 1 -> 0 : ready nextId == 1
                Assertions.assertEquals(1, tenant.currentPageSize());
                Assertions.assertEquals(1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(2, tenant.currentPageSize());
                Assertions.assertEquals(2, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                // move to the next page
                for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY; i++) {
                    tenant.nextId();
                }
                Assertions.assertEquals(3, tenant.currentPageSize());
                Assertions.assertEquals(3, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                tenant.freeId(1);
                Assertions.assertEquals(4, tenant.currentPageSize());
                Assertions.assertEquals(ConcurrentPool.PAGE_CAPACITY - 1, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(4, tenant.nextId() & ConcurrentPool.OBJECT_INDEX_BIT_MASK);
                Assertions.assertEquals(5, tenant.currentPageSize());
            }
        }

        @Test
        public void concurrentIds() throws InterruptedException {
            ConcurrentPool<LongEntity> concurrentPool = new ConcurrentPool<>();
            try (ConcurrentPool.Tenant<LongEntity> tenant = concurrentPool.newTenant()) {
                final int capacity = 1 << 20;
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
                Assertions.assertEquals(capacity - removed, concurrentPool.size());
                Assertions.assertEquals((int) (capacity * .9), tenant.nextId());
            }
        }

        @Test
        public void iterator() {
            try (ConcurrentPool.Tenant<Id> tenant = new ConcurrentPool<Id>().newTenant()) {
                for (int i = 0; i < 1_000_000; i++) {
                    long nextId = tenant.nextId();
                    tenant.register(nextId, new Id(i));
                }
                Iterator<Id> iterator = tenant.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    long id = iterator.next().id;
                    Assertions.assertEquals(i++, id);
                }
            }
        }

        public record Id(long id) implements ConcurrentPool.Identifiable {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public long setId(long id) {
                return id;
            }
        }
    }

    @Nested
    public class LinkedPageTest {

        @Test
        public void size() {
            ConcurrentPool.LinkedPage<LongEntity> page = new ConcurrentPool.LinkedPage<>(0, null);
            Assertions.assertEquals(0, page.incrementIndex());
            Assertions.assertEquals(1, page.incrementIndex());
        }

        @Test
        public void capacity() {
            ConcurrentPool.LinkedPage<LongEntity> page = new ConcurrentPool.LinkedPage<>(0, null);
            Assertions.assertTrue(page.hasCapacity());
            for (int i = 0; i < ConcurrentPool.PAGE_CAPACITY - 1; i++) {
                page.incrementIndex();
            }
            Assertions.assertTrue(page.hasCapacity());
            page.incrementIndex();
            Assertions.assertFalse(page.hasCapacity());
        }

        @Test
        public void data() {
            ConcurrentPool.LinkedPage<LongEntity> previous = new ConcurrentPool.LinkedPage<>(0, null);
            ConcurrentPool.LinkedPage<LongEntity> page = new ConcurrentPool.LinkedPage<>(0, previous);
            LongEntity entity = new LongEntity(1, null);
            page.set(10, entity);
            Assertions.assertEquals(entity, page.get(10));
            Assertions.assertEquals(previous, page.getPrevious());
        }
    }
}
