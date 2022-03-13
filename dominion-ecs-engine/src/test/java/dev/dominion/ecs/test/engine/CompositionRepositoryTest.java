package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.CompositionRepository;
import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.IntEntity;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.system.HashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class CompositionRepositoryTest {

    @Test
    void init() {
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(0, 1, 1, 1)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(10, idSchema.chunkBit());
            Assertions.assertEquals(6, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(0, 100, 100, 1)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(24, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(24, idSchema.chunkBit());
            Assertions.assertEquals(6, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(0, 1, 22, 10)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(22, idSchema.chunkBit());
            Assertions.assertEquals(8, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(0, 1, 1, 100)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(10, idSchema.chunkBit());
            Assertions.assertEquals(20, idSchema.chunkCountBit());
        }
    }

    @Test
    void getOrCreate() {
        try (CompositionRepository compositionRepository = new CompositionRepository()) {
            Composition composition = compositionRepository.getOrCreate(new Object[0]);
            Assertions.assertArrayEquals(new Class<?>[0], composition.getComponentTypes());
            CompositionRepository.Node root = compositionRepository.getRoot();
            Assertions.assertEquals(composition, root.getComposition());
        }
    }

    @Test
    void getOrCreateWith1Component() {
        try (CompositionRepository compositionRepository = new CompositionRepository()) {
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1(0)});
            Assertions.assertArrayEquals(new Class<?>[]{C1.class}, composition.getComponentTypes());
            Assertions.assertTrue(compositionRepository.getNodeCache()
                    .contains(HashCode.longHashCode(new int[]{
                            compositionRepository.getClassIndex().getIndex(C1.class)
                    })));
        }
    }

    @Test
    void getOrCreateWith2Component() {
        try (CompositionRepository compositionRepository = new CompositionRepository()) {
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0)});
            Assertions.assertArrayEquals(new Class<?>[]{C1.class, C2.class}, composition.getComponentTypes());
            long longHashCode = HashCode.longHashCode(new int[]{
                    compositionRepository.getClassIndex().getIndex(C1.class)
                    , compositionRepository.getClassIndex().getIndex(C2.class)
            });
            CompositionRepository.NodeCache nodeCache = compositionRepository.getNodeCache();
            Assertions.assertTrue(nodeCache.contains(compositionRepository.getClassIndex().getIndex(C1.class)));
            Assertions.assertTrue(nodeCache.contains(compositionRepository.getClassIndex().getIndex(C2.class)));
            Assertions.assertTrue(nodeCache.contains(longHashCode));
            CompositionRepository.Node nodeC1 = nodeCache.getNode(compositionRepository.getClassIndex().getIndex(C1.class));
            CompositionRepository.Node nodeC2 = nodeCache.getNode(compositionRepository.getClassIndex().getIndex(C2.class));
            Assertions.assertTrue(nodeC1.getLinkedNodes().containsKey(longHashCode));
            Assertions.assertTrue(nodeC2.getLinkedNodes().containsKey(longHashCode));
        }
    }

    @Test
    void find() {
        try (CompositionRepository compositionRepository = new CompositionRepository()) {
            Composition compositionC1 = compositionRepository.getOrCreate(new Object[]{new C1(0)});
            Composition compositionC1C2 = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0)});
            Composition compositionC2C3 = compositionRepository.getOrCreate(new Object[]{new C2(0), new C3(0)});
            Composition compositionC2 = compositionRepository.getOrCreate(new Object[]{new C2(0)});

            Collection<CompositionRepository.Node> nodes = compositionRepository.find(C1.class);
            Assertions.assertNotNull(nodes);
            List<CompositionRepository.Node> nodeList = nodes.stream().toList();
            Assertions.assertEquals(2, nodeList.size());
            Assertions.assertEquals(compositionC1, nodeList.get(0).getComposition());
            Assertions.assertEquals(compositionC1C2, nodeList.get(1).getComposition());

            nodes = compositionRepository.find(C2.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(3, nodeList.size());
            Assertions.assertEquals(compositionC1C2, nodeList.get(0).getComposition());
            Assertions.assertEquals(compositionC2C3, nodeList.get(1).getComposition());
            Assertions.assertEquals(compositionC2, nodeList.get(2).getComposition());

            nodes = compositionRepository.find(C3.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC2C3, nodeList.get(0).getComposition());

            nodes = compositionRepository.find(C2.class, C1.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC1C2, nodeList.get(0).getComposition());

            nodes = compositionRepository.find(C3.class, C2.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC2C3, nodeList.get(0).getComposition());

            nodes = compositionRepository.find(C3.class, C1.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(0, nodeList.size());
        }
    }

    @Test
    void addComponents() {
        var c1 = new C1(0);
        var c2 = new C2(0);
        var c3 = new C3(0);
        var c4 = new C4(0);
        try (EntityRepository entityRepository = (EntityRepository) new EntityRepository.Factory().create("test")) {
            IntEntity entity = (IntEntity) entityRepository.createEntity();
            CompositionRepository repository = entity.getComposition().getRepository();
            IntEntity entityPostAdd = (IntEntity) repository.addComponents(entity, c1);
            Assertions.assertEquals(entityPostAdd, entity);
            Assertions.assertEquals(c1, entityPostAdd.getComponents()[0]);

            entity = (IntEntity) entityRepository.createEntity(c1);
            entityPostAdd = (IntEntity) repository.addComponents(entity);
            Assertions.assertEquals(c1, entityPostAdd.getComponents()[0]);

            entityPostAdd = (IntEntity) repository.addComponents(entity, c2);
            Assertions.assertArrayEquals(new Object[]{c1, c2}, entityPostAdd.getComponents());

            entity = (IntEntity) entityRepository.createEntity(c1, c2);
            entityPostAdd = (IntEntity) repository.addComponents(entity, c3);
            Assertions.assertArrayEquals(new Object[]{c1, c2, c3}, entityPostAdd.getComponents());

            entity = (IntEntity) entityRepository.createEntity(c1);
            entityPostAdd = (IntEntity) repository.addComponents(entity, c2, c3);
            Assertions.assertArrayEquals(new Object[]{c1, c2, c3}, entityPostAdd.getComponents());

            entity = (IntEntity) entityRepository.createEntity(c1, c2);
            entityPostAdd = (IntEntity) repository.addComponents(entity, c3, c4);
            Assertions.assertArrayEquals(new Object[]{c1, c2, c3, c4}, entityPostAdd.getComponents());
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

    @Nested
    public class NodeTest {
        @Test
        void getOrCreateCompositions() {
            try (CompositionRepository compositionRepository = new CompositionRepository()) {
                CompositionRepository.Node node = compositionRepository.new Node(C1.class, C2.class);
                Composition composition = node.getOrCreateComposition();
                Assertions.assertArrayEquals(new Class<?>[]{C1.class, C2.class}, composition.getComponentTypes());
            }
        }

        @Test
        void toStringTest() {
            try (CompositionRepository compositionRepository = new CompositionRepository()) {
                CompositionRepository.Node node = compositionRepository.new Node(C1.class, C2.class);
                Assertions.assertEquals("C1,C2", node.toString());
            }
        }
    }
}
