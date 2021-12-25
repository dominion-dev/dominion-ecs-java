package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompositionTest {

    @Test
    void createEntity() {
        ConcurrentPool<LongEntity> concurrentPool = new ConcurrentPool<>();
        try (ConcurrentPool.Tenant<LongEntity> tenant = concurrentPool.newTenant()) {
            Composition composition = new Composition(tenant);
            LongEntity entity = composition.createEntity();
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(tenant, entity.getTenant());
            LongEntity entry = concurrentPool.getEntry(entity.getId());
            Assertions.assertNotNull(entry);
            Assertions.assertEquals(entity, entry);
        }
    }
}
