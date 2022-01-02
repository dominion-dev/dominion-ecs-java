package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityRepositoryTest {

    @Test
    void createEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity = (LongEntity) entityRepository.createEntity();
        Assertions.assertNotNull(entity.getComposition());
        Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
    }

    @Test
    void createEntityWith1Component() {
        EntityRepository entityRepository = new EntityRepository();
        C1 c1 = new C1();
        LongEntity entity = (LongEntity) entityRepository.createEntity(c1);
        Assertions.assertNotNull(entity.getComposition());
        Assertions.assertEquals(c1, entity.getSingleComponent());
        Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
    }

    @Test
    void destroyEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity = (LongEntity) entityRepository.createEntity();
        ConcurrentPool<LongEntity> pool = entity.getComposition().getTenant().getPool();
        entityRepository.destroyEntity(entity);
        Assertions.assertNull(entity.getComposition());
        Assertions.assertNull(pool.getEntry(entity.getId()));
    }

    @Test
    void avoidEmptyPositionOnDestroyEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity1 = (LongEntity) entityRepository.createEntity();
        LongEntity entity2 = (LongEntity) entityRepository.createEntity();
        ConcurrentPool<LongEntity> pool = entity1.getComposition().getTenant().getPool();
        long id1 = entity1.getId();
        long id2 = entity2.getId();
        entityRepository.destroyEntity(entity1);
        Assertions.assertNull(pool.getEntry(id2));
        Assertions.assertEquals(entity2, pool.getEntry(id1));
        Assertions.assertEquals(id1, entity2.getId());
    }

    private static class C1 {
    }
}