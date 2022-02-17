package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.LongEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LongEntityTest {

    @Test
    void add() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            LongEntity entity = (LongEntity) entityRepository.createEntity();
            Assertions.assertNull(entity.getSingleComponent());
            Assertions.assertNull(entity.getComponents());
            entity.add(c1);
            Assertions.assertEquals(c1, entity.getSingleComponent());
            Assertions.assertNull(entity.getComponents());
            entity.add(c2);
            Assertions.assertNull(entity.getSingleComponent());
            Assertions.assertArrayEquals(new Object[]{c1, c2}, entity.getComponents());
        }
    }

    @Test
    void remove() {
    }

    @Test
    void has() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            LongEntity entity = (LongEntity) entityRepository.createEntity();
            Assertions.assertFalse(entity.has(C1.class));
            LongEntity entity2 = (LongEntity) entityRepository.createEntity(c1);
            Assertions.assertTrue(entity2.has(C1.class));
            entity.add(c1);
            Assertions.assertTrue(entity.has(C1.class));
            entity.add(c2);
            Assertions.assertTrue(entity.has(C1.class));
            Assertions.assertTrue(entity.has(C2.class));
        }
    }

    @Test
    void contains() {
        try (EntityRepository entityRepository = new EntityRepository()) {
            var c1 = new C1(0);
            var c2 = new C2(0);
            LongEntity entity = (LongEntity) entityRepository.createEntity();
            Assertions.assertFalse(entity.contains(c1));
            LongEntity entity2 = (LongEntity) entityRepository.createEntity(c1);
            Assertions.assertTrue(entity2.contains(c1));
            entity.add(c1);
            Assertions.assertTrue(entity.contains(c1));
            entity.add(c2);
            Assertions.assertTrue(entity.contains(c1));
            Assertions.assertTrue(entity.contains(c2));
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
