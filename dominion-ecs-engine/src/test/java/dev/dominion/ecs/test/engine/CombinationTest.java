package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Combination;
import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CombinationTest {

    @Test
    void createEntity() {
        ConcurrentPool<LongEntity> concurrentPool = new ConcurrentPool<>();
        try (ConcurrentPool.Tenant<LongEntity> tenant = concurrentPool.newTenant()) {
            Combination combination = new Combination(tenant);
            LongEntity entity = combination.createEntity();
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(tenant, entity.getTenant());
            LongEntity entry = concurrentPool.getEntry(entity.getId());
            Assertions.assertNotNull(entry);
            Assertions.assertEquals(entity, entry);
        }
    }
}
