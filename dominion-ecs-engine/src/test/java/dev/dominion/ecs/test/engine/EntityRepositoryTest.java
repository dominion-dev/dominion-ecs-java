package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityRepositoryTest {

    @Test
    void createEntity() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            Assertions.assertNotNull(entity.getComposition());
            Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
        }
    }

    @Test
    void createEntityWith1Component() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            C1 c1 = new C1(0);
            IntEntity entity = (IntEntity) entityRepository.createEntity(c1);
            Assertions.assertNotNull(entity.getComposition());
            Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
            Assertions.assertEquals(c1, entity.getComponents()[0]);
        }
    }

    @Test
    void createEntityWith2Component() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            IntEntity entity1 = (IntEntity) entityRepository.createEntity(c1, c2);
            Assertions.assertNotNull(entity1.getComposition());
            Assertions.assertEquals(entity1.getComposition().getTenant().getPool().getEntry(entity1.getId()), entity1);
            Assertions.assertArrayEquals(new Object[]{c1, c2}, entity1.getComponents());
            IntEntity entity2 = (IntEntity) entityRepository.createEntity(c2, c1);
            Assertions.assertArrayEquals(new Object[]{c1, c2}, entity2.getComponents());
        }
    }

    @Test
    void deleteEntity() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            ChunkedPool<IntEntity> pool = entity.getComposition().getTenant().getPool();
            entityRepository.deleteEntity(entity);
            Assertions.assertNull(entity.getData());
            Assertions.assertNull(pool.getEntry(entity.getId()));
        }
    }

    @Test
    void avoidEmptyPositionOnDestroyEntity() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity1 = (IntEntity) entityRepository.createEntity();
            IntEntity entity2 = (IntEntity) entityRepository.createEntity();
            ChunkedPool<IntEntity> pool = entity1.getComposition().getTenant().getPool();
            int id1 = entity1.getId();
            int id2 = entity2.getId();
            entityRepository.deleteEntity(entity1);
            Assertions.assertNull(pool.getEntry(id2));
            Assertions.assertEquals(entity2, pool.getEntry(id1));
            Assertions.assertEquals(id1, entity2.getId());
        }
    }

    @Test
    void findComponents() {
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        var c4 = new C4(0);
        var c5 = new C5(0);
        var c6 = new C6(0);
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            entityRepository.createEntity(c1);
            entityRepository.createEntity(c1, c2);
            entityRepository.createEntity(c1, c2, c3);
            entityRepository.createEntity(c1, c2, c3, c4);
            entityRepository.createEntity(c1, c2, c3, c4, c5);
            entityRepository.createEntity(c1, c2, c3, c4, c5, c6);

            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class).iterator().next().comp());
            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class, C2.class).iterator().next().comp1());
            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class, C2.class, C3.class).iterator().next().comp1());
            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class).iterator().next().comp1());
            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp1());
            Assertions.assertEquals(c1, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp1());
            Assertions.assertEquals(c2, entityRepository.findComponents(C1.class, C2.class).iterator().next().comp2());
            Assertions.assertEquals(c2, entityRepository.findComponents(C1.class, C2.class, C3.class).iterator().next().comp2());
            Assertions.assertEquals(c2, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class).iterator().next().comp2());
            Assertions.assertEquals(c2, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp2());
            Assertions.assertEquals(c2, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp2());
            Assertions.assertEquals(c3, entityRepository.findComponents(C1.class, C2.class, C3.class).iterator().next().comp3());
            Assertions.assertEquals(c3, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class).iterator().next().comp3());
            Assertions.assertEquals(c3, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp3());
            Assertions.assertEquals(c3, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp3());
            Assertions.assertEquals(c4, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class).iterator().next().comp4());
            Assertions.assertEquals(c4, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp4());
            Assertions.assertEquals(c4, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp4());
            Assertions.assertEquals(c5, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp5());
            Assertions.assertEquals(c5, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp5());
            Assertions.assertEquals(c6, entityRepository.findComponents(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp6());
        }
    }

    @Test
    void findComponents1FromMoreCompositions() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity1 = (IntEntity) entityRepository.createEntity(new C1(0));
            IntEntity entity2 = (IntEntity) entityRepository.createEntity(new C1(1), new C2(2));

            var results = entityRepository.findComponents(C1.class);
            Assertions.assertNotNull(results);
            var iterator = results.iterator();
            Assertions.assertNotNull(iterator);
            Assertions.assertTrue(iterator.hasNext());
            var next = iterator.next();
            Assertions.assertEquals(0, next.comp().id);
            Assertions.assertEquals(entity1, next.entity());
            Assertions.assertTrue(iterator.hasNext());
            next = iterator.next();
            Assertions.assertEquals(1, next.comp().id);
            Assertions.assertEquals(entity2, next.entity());

            var results2 = entityRepository.findComponents(C2.class);
            var iterator2 = results2.iterator();
            Assertions.assertNotNull(iterator2);
            Assertions.assertTrue(iterator2.hasNext());
            var next2 = iterator2.next();
            Assertions.assertEquals(2, next2.comp().id);
            Assertions.assertEquals(entity2, next2.entity());

            var results3 = entityRepository.findComponents(C3.class);
            var iterator3 = results3.iterator();
            Assertions.assertNotNull(iterator3);
            Assertions.assertFalse(iterator3.hasNext());
        }
    }

    @Test
    void findComponents2FromMoreCompositions() {
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity1 = (IntEntity) entityRepository.createEntity(new C1(1), new C2(2));
            IntEntity entity2 = (IntEntity) entityRepository.createEntity(new C1(3), new C2(4), new C3(5));

            var results = entityRepository.findComponents(C1.class, C2.class);
            Assertions.assertNotNull(results);
            var iterator = results.iterator();
            Assertions.assertNotNull(iterator);
            Assertions.assertTrue(iterator.hasNext());
            var next = iterator.next();
            Assertions.assertEquals(entity2, next.entity());
            Assertions.assertEquals(3, next.comp1().id);
            Assertions.assertEquals(4, next.comp2().id);
            Assertions.assertTrue(iterator.hasNext());
            next = iterator.next();
            Assertions.assertEquals(entity1, next.entity());
            Assertions.assertEquals(1, next.comp1().id);
            Assertions.assertEquals(2, next.comp2().id);

            var results2 = entityRepository.findComponents(C2.class, C3.class);
            var iterator2 = results2.iterator();
            Assertions.assertNotNull(iterator2);
            Assertions.assertTrue(iterator2.hasNext());
            var next2 = iterator2.next();
            Assertions.assertEquals(4, next2.comp1().id);
            Assertions.assertEquals(5, next2.comp2().id);
            Assertions.assertEquals(entity2, next2.entity());
        }
    }

    record C1(int id) {
    }

    record C2(int id) {
    }

    record C3(int id) {
    }

    record C4(int id) {
    }

    record C5(int id) {
    }

    record C6(int id) {
    }
}
