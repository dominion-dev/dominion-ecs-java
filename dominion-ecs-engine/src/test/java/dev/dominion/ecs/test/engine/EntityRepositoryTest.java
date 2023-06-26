package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Composition;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class EntityRepositoryTest {

    @Test
    void factoryCreate() {
        Assertions.assertEquals(EntityRepository.class, Dominion.factory().create().getClass());
    }

    @Test
    void createEntity() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        IntEntity entity = (IntEntity) entityRepository.createEntity();
        Assertions.assertNotNull(entity.getChunk());
        Assertions.assertEquals(entity.getChunk().getTenant().getPool().getEntry(entity.getId()), entity);
    }

    @Test
    public void concurrentCreateEntity() throws InterruptedException {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("stress-test");
        final int capacity = 1 << 22;
        final ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < capacity; i++) {
            pool.execute(() -> entityRepository.createEntity(new C1(0), new C2(0)));
        }
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        var count = new AtomicInteger(0);
        for (var rs : entityRepository.findEntitiesWith(C1.class)) {
            Assertions.assertNotNull(rs.entity());
            Assertions.assertNotNull(rs.comp());
            count.getAndIncrement();
        }
        Assertions.assertEquals(capacity, count.get());
    }

    @Test
    void createAndDeleteEntityBulk() {
        int capacity = 1_000_000;
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("stress-test");
        Entity[] entities = new Entity[capacity];
        for (int i = 0; i < capacity; i++) {
            entities[i] = entityRepository.createEntity(new C1(1), new C2(2), new C3(3));
        }
        System.out.println("new entities created: " + capacity);
        for (int i = 0; i < capacity; i++) {
            entityRepository.deleteEntity(entities[i]);
        }
        System.out.println("entities deleted: " + capacity);
        for (int i = 0; i < capacity; i++) {
            entities[i] = entityRepository.createEntity(new C1(1), new C2(2), new C3(3));
        }
        System.out.println("entities recreated: " + capacity);
        for (int i = 0; i < capacity; i++) {
            entityRepository.deleteEntity(entities[i]);
        }
        System.out.println("entities deleted: " + capacity);
        for (int i = 0; i < capacity; i++) {
            entities[i] = entityRepository.createEntity(new C1(1), new C2(2), new C3(3));
        }
        System.out.println("entities recreated: " + capacity);
    }

    @Test
    void createEntityWith1Component() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        C1 c1 = new C1(0);
        IntEntity entity = (IntEntity) entityRepository.createEntity(c1);
        Assertions.assertNotNull(entity.getChunk());
        Assertions.assertEquals(entity.getChunk().getTenant().getPool().getEntry(entity.getId()), entity);
        Assertions.assertEquals(c1, Objects.requireNonNull(entity.getComponentArray())[0]);

    }

    @Test
    void createPreparedEntityWith1Component() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        C1 c1 = new C1(0);
        Composition.Of1<C1> ofC1 = entityRepository.composition().of(C1.class);
        IntEntity entity = (IntEntity) entityRepository.createPreparedEntity(ofC1.withValue(c1));
        Assertions.assertNotNull(entity.getChunk());
        Assertions.assertEquals(entity.getChunk().getTenant().getPool().getEntry(entity.getId()), entity);
        Assertions.assertEquals(c1, Objects.requireNonNull(entity.getComponentArray())[0]);
    }

    @Test
    void createEntityWith2Component() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        var c1 = new C1(0);
        var c2 = new C2(0);
        IntEntity entity1 = (IntEntity) entityRepository.createEntity(c1, c2);
        Assertions.assertNotNull(entity1.getChunk());
        Assertions.assertEquals(entity1.getChunk().getTenant().getPool().getEntry(entity1.getId()), entity1);
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity1.getComponentArray());
        IntEntity entity2 = (IntEntity) entityRepository.createEntity(c2, c1);
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity2.getComponentArray());
    }

    @Test
    void createPreparedEntityWith2Component() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        var c1 = new C1(0);
        var c2 = new C2(0);
        Composition.Of2<C1, C2> ofC1C2 = entityRepository.composition().of(C1.class, C2.class);
        Composition.Of2<C2, C1> ofC2C1 = entityRepository.composition().of(C2.class, C1.class);
        IntEntity entity1 = (IntEntity) entityRepository.createPreparedEntity(ofC1C2.withValue(c1, c2));
        Assertions.assertNotNull(entity1.getChunk());
        Assertions.assertEquals(entity1.getChunk().getTenant().getPool().getEntry(entity1.getId()), entity1);
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity1.getComponentArray());
        IntEntity entity2 = (IntEntity) entityRepository.createPreparedEntity(ofC2C1.withValue(c2, c1));
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity2.getComponentArray());
    }

    @Test
    void createEntityAs() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        IntEntity entity0 = (IntEntity) entityRepository.createEntity();
        var c1 = new C1(0);
        var c2 = new C2(0);
        IntEntity entity1 = (IntEntity) entityRepository.createEntityAs(entity0, c1);
        Assertions.assertNotNull(entity1.getChunk());
        Assertions.assertEquals(entity1.getChunk().getTenant().getPool().getEntry(entity1.getId()), entity1);
        Assertions.assertArrayEquals(new Object[]{c1}, entity1.getComponentArray());
        IntEntity entity2 = (IntEntity) entityRepository.createEntityAs(entity1, c2);
        Assertions.assertArrayEquals(new Object[]{c2, c1}, entity2.getComponentArray());
    }

    @Test
    void deleteEntity() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        C1 c10 = new C1(0);
        IntEntity entity = (IntEntity) entityRepository.createEntity(c10);
        Assertions.assertTrue(entityRepository.deleteEntity(entity));
        Assertions.assertTrue(entity.isDeleted());
        Assertions.assertFalse(entity.isEnabled());
        Assertions.assertArrayEquals(null, entity.getComponentArray());
        Assertions.assertNull(entity.getChunk());
        Assertions.assertNull(entity.getStateChunk());
    }

    @Test
    void modifyEntity() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        IntEntity entity = (IntEntity) entityRepository.createEntity(c1);
        Assertions.assertArrayEquals(new Object[]{c1}, entity.getComponentArray());
        Composition composition = entityRepository.composition();

        Composition.Modifier modifier = composition.byRemoving(C1.class).withValue(entity);
        Assertions.assertTrue(entityRepository.modifyEntity(modifier));
        Assertions.assertNull(entity.getComponentArray());

        modifier = composition.byAdding1AndRemoving(C1.class).withValue(entity, c1);
        Assertions.assertTrue(entityRepository.modifyEntity(modifier));
        Assertions.assertArrayEquals(new Object[]{c1}, entity.getComponentArray());

        modifier = composition.byAdding1AndRemoving(C2.class, C1.class).withValue(entity, c2);
        Assertions.assertTrue(entityRepository.modifyEntity(modifier));
        Assertions.assertArrayEquals(new Object[]{c2}, entity.getComponentArray());

        modifier = composition.byAdding2AndRemoving(C1.class, C2.class, C2.class).withValue(entity, c1, c2);
        Assertions.assertTrue(entityRepository.modifyEntity(modifier));
        Assertions.assertArrayEquals(new Object[]{c1}, entity.getComponentArray());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> composition.byAdding1AndRemoving(C1.class).withValue(entity, c1));

        entity.setState(DataCompositionTest.State1.ONE);
        ChunkedPool.Tenant<IntEntity> firstTenant = entity.getStateChunk().getTenant();
        Assertions.assertEquals(1, firstTenant.currentChunkSize());
        modifier = composition.byAdding2AndRemoving(C2.class, C3.class).withValue(entity, c2, c3);
        Assertions.assertTrue(entityRepository.modifyEntity(modifier));
        Assertions.assertArrayEquals(new Object[]{c1, c2, c3}, entity.getComponentArray());
        ChunkedPool.Tenant<IntEntity> secondTenant = entity.getStateChunk().getTenant();
        Assertions.assertNotEquals(firstTenant, secondTenant);
        Assertions.assertEquals(0, firstTenant.currentChunkSize());
        Assertions.assertEquals(1, secondTenant.currentChunkSize());
    }

    @Test
    void avoidEmptyPositionOnDestroyEntity() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
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


    @Test
    void findAllEntities() {
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        var c4 = new C4(0);
        var c5 = new C5(0);
        var c6 = new C6(0);
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        Entity[] entities = new Entity[6];
        entities[0] = entityRepository.createEntity(c1);
        entities[1] = entityRepository.createEntity(c1, c2);
        entities[2] = entityRepository.createEntity(c1, c2, c3);
        entities[3] = entityRepository.createEntity(c1, c2, c3, c4);
        entities[4] = entityRepository.createEntity(c1, c2, c3, c4, c5);
        entities[5] = entityRepository.createEntity(c1, c2, c3, c4, c5, c6);

        Iterator<Entity> iterator = entityRepository.findAllEntities().iterator();
        Assertions.assertEquals(entities[5], iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(entities[4], iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(entities[3], iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(entities[2], iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(entities[1], iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(entities[0], iterator.next());
        Assertions.assertFalse(iterator.hasNext());
    }


    @Test
    void findComponents() {
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        var c4 = new C4(0);
        var c5 = new C5(0);
        var c6 = new C6(0);
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(c1);
        entityRepository.createEntity(c1, c2);
        entityRepository.createEntity(c1, c2, c3);
        entityRepository.createEntity(c1, c2, c3, c4);
        entityRepository.createEntity(c1, c2, c3, c4, c5);
        entityRepository.createEntity(c1, c2, c3, c4, c5, c6);

        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class).iterator().next().comp());
        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class, C2.class).iterator().next().comp1());
        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).iterator().next().comp1());
        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class).iterator().next().comp1());
        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp1());
        Assertions.assertEquals(c1, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp1());
        Assertions.assertEquals(c2, entityRepository.findEntitiesWith(C1.class, C2.class).iterator().next().comp2());
        Assertions.assertEquals(c2, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).iterator().next().comp2());
        Assertions.assertEquals(c2, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class).iterator().next().comp2());
        Assertions.assertEquals(c2, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp2());
        Assertions.assertEquals(c2, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp2());
        Assertions.assertEquals(c3, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).iterator().next().comp3());
        Assertions.assertEquals(c3, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class).iterator().next().comp3());
        Assertions.assertEquals(c3, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp3());
        Assertions.assertEquals(c3, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp3());
        Assertions.assertEquals(c4, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class).iterator().next().comp4());
        Assertions.assertEquals(c4, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp4());
        Assertions.assertEquals(c4, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp4());
        Assertions.assertEquals(c5, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class).iterator().next().comp5());
        Assertions.assertEquals(c5, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp5());
        Assertions.assertEquals(c6, entityRepository.findEntitiesWith(C1.class, C2.class, C3.class, C4.class, C5.class, C6.class).iterator().next().comp6());
    }

    @Test
    void findComponentsWithState() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(10)).setState(State.ONE);
        entityRepository.createEntity(new C1(11), new C2(20)).setState(State.ONE);
        entityRepository.createEntity(new C1(12), new C2(21)).setState(State.ONE);
        entityRepository.createEntity(new C1(13), new C2(22)).setState(State.TWO);
        entityRepository.createEntity(new C1(14)).setState(State.TWO);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> entityRepository.findCompositionsWith(C1.class).withState(State.ONE));

        var results1 = entityRepository.findEntitiesWith(C1.class);
        var iterator = results1.withState(State.ONE).iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertTrue(iterator.hasNext());
        var next = iterator.next();
        Assertions.assertEquals(10, next.comp().id);
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(12, iterator.next().comp().id);
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(11, iterator.next().comp().id);
        Assertions.assertFalse(iterator.hasNext());

        var results2 = entityRepository.findEntitiesWith(C1.class, C2.class);
        var iterator2 = results2.withState(State.ONE).iterator();
        Assertions.assertNotNull(iterator2);
        Assertions.assertTrue(iterator2.hasNext());
        var next2 = iterator2.next();
        Assertions.assertEquals(12, next2.comp1().id);
        Assertions.assertEquals(21, next2.comp2().id);
        Assertions.assertTrue(iterator2.hasNext());
        next2 = iterator2.next();
        Assertions.assertEquals(11, next2.comp1().id);
        Assertions.assertEquals(20, next2.comp2().id);
        Assertions.assertFalse(iterator2.hasNext());

        var iterator3 = results2.withState(State.TWO).iterator();
        Assertions.assertNotNull(iterator3);
        Assertions.assertTrue(iterator3.hasNext());
        var next3 = iterator3.next();
        Assertions.assertEquals(13, next3.comp1().id);
        Assertions.assertEquals(22, next3.comp2().id);
        Assertions.assertFalse(iterator3.hasNext());

        var iterator4 = results1.withState(State.TWO).iterator();
        Assertions.assertNotNull(iterator4);
        Assertions.assertTrue(iterator4.hasNext());
        var next4 = iterator4.next();
        Assertions.assertEquals(14, next4.comp().id);
        Assertions.assertTrue(iterator4.hasNext());
        next4 = iterator4.next();
        Assertions.assertEquals(13, next4.comp().id);
        Assertions.assertFalse(iterator4.hasNext());

    }

    @Test
    void findComponents1FromMoreCompositions() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        IntEntity entity1 = (IntEntity) entityRepository.createEntity(new C1(0));
        IntEntity entity2 = (IntEntity) entityRepository.createEntity(new C1(1), new C2(2));

        var results = entityRepository.findEntitiesWith(C1.class);
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

        var results2 = entityRepository.findEntitiesWith(C2.class);
        var iterator2 = results2.iterator();
        Assertions.assertNotNull(iterator2);
        Assertions.assertTrue(iterator2.hasNext());
        var next2 = iterator2.next();
        Assertions.assertEquals(2, next2.comp().id);
        Assertions.assertEquals(entity2, next2.entity());

        var results3 = entityRepository.findEntitiesWith(C3.class);
        var iterator3 = results3.iterator();
        Assertions.assertNotNull(iterator3);
        Assertions.assertFalse(iterator3.hasNext());
    }

    @Test
    void findComponents2FromMoreCompositions() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        IntEntity entity1 = (IntEntity) entityRepository.createEntity(new C1(1), new C2(2));
        IntEntity entity2 = (IntEntity) entityRepository.createEntity(new C1(3), new C2(4), new C3(5));

        var results = entityRepository.findEntitiesWith(C1.class, C2.class);
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

        var results2 = entityRepository.findEntitiesWith(C2.class, C3.class);
        var iterator2 = results2.iterator();
        Assertions.assertNotNull(iterator2);
        Assertions.assertTrue(iterator2.hasNext());
        var next2 = iterator2.next();
        Assertions.assertEquals(4, next2.comp1().id);
        Assertions.assertEquals(5, next2.comp2().id);
        Assertions.assertEquals(entity2, next2.entity());
    }

    @Test
    void findEntitiesByMoreComponentsThanExpected() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0));
        AtomicInteger count = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C2.class).stream().forEach((rs) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());

        AtomicInteger count1 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C2.class).parallelStream().forEach((rs) -> count1.incrementAndGet());
        Assertions.assertEquals(0, count1.get());

        entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0), new C2(0));
        AtomicInteger count2 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).stream().forEach((rs) -> count2.incrementAndGet());
        Assertions.assertEquals(0, count2.get());

        AtomicInteger count3 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C2.class, C3.class).parallelStream().forEach((rs) -> count3.incrementAndGet());
        Assertions.assertEquals(0, count3.get());
    }

    @Test
    void findEntitiesByWrongComposition() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0), new C2(0));
        entityRepository.createEntity(new C3(0), new C4(0));
        AtomicInteger count = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C3.class).stream().forEach((rs) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());

        AtomicInteger count1 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class, C3.class).parallelStream().forEach((rs) -> count1.incrementAndGet());
        Assertions.assertEquals(0, count1.get());
    }

    @Test
    void findEntitiesWithAlsoUnregisteredComponent() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0), new C2(0));
        AtomicInteger count = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class).withAlso(C3.class).stream().forEach((rs) -> count.incrementAndGet());
        Assertions.assertEquals(0, count.get());

        AtomicInteger count1 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class).withAlso(C3.class).parallelStream().forEach((rs) -> count1.incrementAndGet());
        Assertions.assertEquals(0, count1.get());

        AtomicInteger count2 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C3.class).withAlso(C1.class).stream().forEach((rs) -> count2.incrementAndGet());
        Assertions.assertEquals(0, count2.get());

        AtomicInteger count3 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C3.class).withAlso(C1.class).parallelStream().forEach((rs) -> count3.incrementAndGet());
        Assertions.assertEquals(0, count3.get());
    }

    @Test
    void findEntitiesWithoutUnregisteredComponent() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0));
        AtomicInteger count = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class).without(C3.class).stream().forEach((rs) -> count.incrementAndGet());
        Assertions.assertEquals(1, count.get());

        AtomicInteger count1 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C1.class).without(C3.class).parallelStream().forEach((rs) -> count1.incrementAndGet());
        Assertions.assertEquals(1, count1.get());

        AtomicInteger count2 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C3.class).without(C1.class).stream().forEach((rs) -> count2.incrementAndGet());
        Assertions.assertEquals(0, count2.get());

        AtomicInteger count3 = new AtomicInteger(0);
        entityRepository.findEntitiesWith(C3.class).without(C1.class).parallelStream().forEach((rs) -> count3.incrementAndGet());
        Assertions.assertEquals(0, count3.get());
    }

    @Test
    void findEntitiesAddingAndRemovingComponents() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");

        var entity = entityRepository.createEntity(new C1(0));
        var iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertTrue(iterator.hasNext());

        entity.add(new C2(0));
        iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertTrue(iterator.hasNext());

        entity.add(new C3(0));
        iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertTrue(iterator.hasNext());

        entity.removeType(C3.class);
        iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertTrue(iterator.hasNext());
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void findAndDisableEntities() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        entityRepository.createEntity(new C1(0));
        entityRepository.createEntity(new C1(1));
        entityRepository.createEntity(new C1(2));
        entityRepository.createEntity(new C1(3));
        var iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            var next = iterator.next();
            next.entity().setEnabled(false);
            System.out.println("next.comp() = " + next.comp());
        }
        iterator = entityRepository.findEntitiesWith(C1.class).iterator();
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    void close() {
        EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test");
        Assertions.assertFalse(entityRepository.isClosed());
        entityRepository.close();
        Assertions.assertTrue(entityRepository.isClosed());
        Assertions.assertThrows(IllegalStateException.class, entityRepository::createEntity);
    }

    enum State {
        ONE, TWO
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
