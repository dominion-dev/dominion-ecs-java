package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ChunkedPoolTest {
    private static final ChunkedPool.IdSchema ID_SCHEMA =
            new ChunkedPool.IdSchema(ConfigSystem.DEFAULT_CHUNK_BIT, ConfigSystem.DEFAULT_CHUNK_COUNT_BIT);

    @Test
    public void newTenant() {
        try (ChunkedPool.Tenant<IntEntity> tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.DEFAULT).newTenant()) {
            Assertions.assertNotNull(tenant);
        }
    }

    @Test
    public void register() {
        ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.DEFAULT);
        try (ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant()) {
            IntEntity entry = new IntEntity(1, null);
            Assertions.assertEquals(entry, tenant.register(1, entry));
            Assertions.assertEquals(entry, chunkedPool.getEntry(1));
        }
    }

    @Nested
    public class TenantTest {

        @Test
        public void nextId() {
            try (ChunkedPool.Tenant<IntEntity> tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.DEFAULT).newTenant()) {
                Assertions.assertEquals(0, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(1, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(0, (tenant.nextId() >> ID_SCHEMA.chunkBit())
                        & ID_SCHEMA.chunkIdBitMask());
                for (int i = 0; i < ID_SCHEMA.chunkCapacity(); i++) {
                    tenant.nextId();
                }
                Assertions.assertEquals(1, (tenant.nextId() >> ID_SCHEMA.chunkBit())
                        & ID_SCHEMA.chunkIdBitMask());
                Assertions.assertEquals(4, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
            }
        }

        @Test
        public void freeId() {
            try (ChunkedPool.Tenant<IntEntity> tenant = new ChunkedPool<IntEntity>(ID_SCHEMA, LoggingSystem.Context.DEFAULT).newTenant()) {
                Assertions.assertEquals(0, tenant.currentChunkSize());
                Assertions.assertEquals(0, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(1, tenant.currentChunkSize());
                Assertions.assertEquals(1, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(2, tenant.currentChunkSize()); // ready nextId == 2
                tenant.freeId(0); // 1 -> 0 : ready nextId == 1
                Assertions.assertEquals(1, tenant.currentChunkSize());
                Assertions.assertEquals(1, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(2, tenant.currentChunkSize());
                Assertions.assertEquals(2, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                // move to the next chunk
                for (int i = 0; i < ID_SCHEMA.chunkCapacity(); i++) {
                    tenant.nextId();
                }
                Assertions.assertEquals(3, tenant.currentChunkSize());
                Assertions.assertEquals(3, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                tenant.freeId(1);
                Assertions.assertEquals(4, tenant.currentChunkSize());
                Assertions.assertEquals(ID_SCHEMA.chunkCapacity() - 1, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(4, tenant.nextId() & ID_SCHEMA.objectIdBitMask());
                Assertions.assertEquals(5, tenant.currentChunkSize());
            }
        }

        @Test
        public void concurrentIds() throws InterruptedException {
            ChunkedPool<IntEntity> chunkedPool = new ChunkedPool<>(ID_SCHEMA, LoggingSystem.Context.DEFAULT);
            try (ChunkedPool.Tenant<IntEntity> tenant = chunkedPool.newTenant()) {
                final int capacity = 1 << 20;
                final ExecutorService pool = Executors.newFixedThreadPool(2);
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
                Assertions.assertEquals(capacity - removed, chunkedPool.size());
                Assertions.assertEquals((int) (capacity * .9), tenant.nextId());
            }
        }

        @Test
        public void iterator() {
            try (ChunkedPool.Tenant<Id> tenant = new ChunkedPool<Id>(ID_SCHEMA, LoggingSystem.Context.DEFAULT).newTenant()) {
                for (int i = 0; i < 1_000_000; i++) {
                    int nextId = tenant.nextId();
                    tenant.register(nextId, new Id(i, -1, -1));
                }
                Iterator<Id> iterator = tenant.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    long id = iterator.next().id;
                    Assertions.assertEquals(i++, id);
                }
            }
        }

        public record Id(int id, int prevId, int nextId) implements ChunkedPool.Identifiable {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public int setId(int id) {
                return id;
            }

            @Override
            public int getPrevId() {
                return prevId;
            }

            @Override
            public int setPrevId(int prevId) {
                return prevId;
            }

            @Override
            public int getNextId() {
                return nextId;
            }

            @Override
            public int setNextId(int nextId) {
                return nextId;
            }
        }
    }

    @Nested
    public class LinkedChunkTest {

        @Test
        public void size() {
            ChunkedPool.LinkedChunk<IntEntity> chunk =
                    new ChunkedPool.LinkedChunk<>(0, ID_SCHEMA, null);
            Assertions.assertEquals(0, chunk.incrementIndex());
            Assertions.assertEquals(1, chunk.incrementIndex());
        }

        @Test
        public void capacity() {
            ChunkedPool.LinkedChunk<IntEntity> chunk =
                    new ChunkedPool.LinkedChunk<>(0, ID_SCHEMA, null);
            Assertions.assertTrue(chunk.hasCapacity());
            for (int i = 0; i < ID_SCHEMA.chunkCapacity() - 1; i++) {
                chunk.incrementIndex();
            }
            Assertions.assertTrue(chunk.hasCapacity());
            chunk.incrementIndex();
            Assertions.assertFalse(chunk.hasCapacity());
        }

        @Test
        public void data() {
            ChunkedPool.LinkedChunk<IntEntity> previous =
                    new ChunkedPool.LinkedChunk<>(0, ID_SCHEMA, null);
            ChunkedPool.LinkedChunk<IntEntity> chunk =
                    new ChunkedPool.LinkedChunk<>(0, ID_SCHEMA, previous);
            IntEntity entity = new IntEntity(1, null);
            chunk.set(10, entity);
            Assertions.assertEquals(entity, chunk.get(10));
            Assertions.assertEquals(previous, chunk.getPrevious());
        }
    }
}
