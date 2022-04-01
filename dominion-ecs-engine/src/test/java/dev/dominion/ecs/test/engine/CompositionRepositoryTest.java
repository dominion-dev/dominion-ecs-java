package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.CompositionRepository;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.LoggingSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CompositionRepositoryTest {

    @Test
    void init() {
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(1, 1, 1
                             , LoggingSystem.Context.TEST)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(10, idSchema.chunkBit());
            Assertions.assertEquals(6, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(100, 100, 1
                             , LoggingSystem.Context.TEST)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(24, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(24, idSchema.chunkBit());
            Assertions.assertEquals(6, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(1, 22, 10
                             , LoggingSystem.Context.TEST)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(22, idSchema.chunkBit());
            Assertions.assertEquals(8, idSchema.chunkCountBit());
        }
        try (CompositionRepository compositionRepository =
                     new CompositionRepository(1, 1, 100
                             , LoggingSystem.Context.TEST)) {
            IdSchema idSchema = compositionRepository.getIdSchema();
            Assertions.assertEquals(14, compositionRepository.getClassIndex().getHashBit());
            Assertions.assertEquals(10, idSchema.chunkBit());
            Assertions.assertEquals(20, idSchema.chunkCountBit());
        }
    }

    @Test
    void getOrCreate() {
        try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
            Composition composition = compositionRepository.getOrCreate(new Object[0]);
            Assertions.assertArrayEquals(new Class<?>[0], composition.getComponentTypes());
            CompositionRepository.Node root = compositionRepository.getRoot();
            Assertions.assertEquals(composition, root.getComposition());
        }
    }

    @Test
    void getOrCreateWith1Component() {
        try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1(0)});
            Assertions.assertArrayEquals(new Class<?>[]{C1.class}, composition.getComponentTypes());
            Assertions.assertTrue(compositionRepository.getNodeCache()
                    .contains(new IndexKey(compositionRepository.getClassIndex().getIndex(C1.class))));
        }
    }

    @Test
    void getOrCreateWith2Component() {
        try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0)});
            Assertions.assertArrayEquals(new Class<?>[]{C1.class, C2.class}, composition.getComponentTypes());
            IndexKey indexKey = new IndexKey(new int[]{
                    compositionRepository.getClassIndex().getIndex(C1.class)
                    , compositionRepository.getClassIndex().getIndex(C2.class)
            });
            CompositionRepository.NodeCache nodeCache = compositionRepository.getNodeCache();
            Assertions.assertTrue(nodeCache.contains(new IndexKey(compositionRepository.getClassIndex().getIndex(C1.class))));
            Assertions.assertTrue(nodeCache.contains(new IndexKey(compositionRepository.getClassIndex().getIndex(C2.class))));
            Assertions.assertTrue(nodeCache.contains(indexKey));
            CompositionRepository.Node nodeC1 = nodeCache.getNode(new IndexKey(compositionRepository.getClassIndex().getIndex(C1.class)));
            CompositionRepository.Node nodeC2 = nodeCache.getNode(new IndexKey(compositionRepository.getClassIndex().getIndex(C2.class)));
            Assertions.assertTrue(nodeC1.getLinkedNodes().containsKey(indexKey));
            Assertions.assertTrue(nodeC2.getLinkedNodes().containsKey(indexKey));
        }
    }

    @Test
    void findWith() {
        try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
            ClassIndex classIndex = compositionRepository.getClassIndex();
            Composition compositionC1 = compositionRepository.getOrCreate(new Object[]{new C1(0)});
            Composition compositionC1C2 = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0)});
            Composition compositionC2C3 = compositionRepository.getOrCreate(new Object[]{new C2(0), new C3(0)});
            Composition compositionC2 = compositionRepository.getOrCreate(new Object[]{new C2(0)});

            IndexKey compositionC1Key = new IndexKey(classIndex.getIndex(C1.class));
            IndexKey compositionC1C2Key = classIndex.getIndexKey(new Object[]{new C1(0), new C2(0)});
            IndexKey compositionC2C3Key = classIndex.getIndexKey(new Object[]{new C2(0), new C3(0)});
            IndexKey compositionC2Key = new IndexKey(classIndex.getIndex(C2.class));

            Map<IndexKey, CompositionRepository.Node> nodeMap = compositionRepository.findWith(C1.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(2, nodeMap.size());
            Assertions.assertEquals(compositionC1, nodeMap.get(compositionC1Key).getComposition());
            Assertions.assertEquals(compositionC1C2, nodeMap.get(compositionC1C2Key).getComposition());

            nodeMap = compositionRepository.findWith(C2.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(3, nodeMap.size());
            Assertions.assertEquals(compositionC1C2, nodeMap.get(compositionC1C2Key).getComposition());
            Assertions.assertEquals(compositionC2C3, nodeMap.get(compositionC2C3Key).getComposition());
            Assertions.assertEquals(compositionC2, nodeMap.get(compositionC2Key).getComposition());

            nodeMap = compositionRepository.findWith(C3.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(1, nodeMap.size());
            Assertions.assertEquals(compositionC2C3, nodeMap.get(compositionC2C3Key).getComposition());

            nodeMap = compositionRepository.findWith(C2.class, C1.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(1, nodeMap.size());
            Assertions.assertEquals(compositionC1C2, nodeMap.get(compositionC1C2Key).getComposition());

            nodeMap = compositionRepository.findWith(C3.class, C2.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(1, nodeMap.size());
            Assertions.assertEquals(compositionC2C3, nodeMap.get(compositionC2C3Key).getComposition());

            nodeMap = compositionRepository.findWith(C3.class, C1.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(0, nodeMap.size());
        }
    }

    @Test
    void removeFrom() {
        try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
            ClassIndex classIndex = compositionRepository.getClassIndex();
            Composition compositionC1 = compositionRepository.getOrCreate(new Object[]{new C1(0)});
            Composition compositionC1C2 = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0)});
            Composition compositionC1C2C3 = compositionRepository.getOrCreate(new Object[]{new C1(0), new C2(0), new C3(0)});

            IndexKey compositionC1Key = new IndexKey(classIndex.getIndex(C1.class));
            IndexKey compositionC1C2Key = classIndex.getIndexKey(new Object[]{new C1(0), new C2(0)});
            IndexKey compositionC1C2C3Key = classIndex.getIndexKey(new Object[]{new C1(0), new C2(0), new C3(0)});

            Map<IndexKey, CompositionRepository.Node> nodeMap = compositionRepository.findWith(C1.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(3, nodeMap.size());
            Assertions.assertEquals(compositionC1, nodeMap.get(compositionC1Key).getComposition());
            Assertions.assertEquals(compositionC1C2, nodeMap.get(compositionC1C2Key).getComposition());
            Assertions.assertEquals(compositionC1C2C3, nodeMap.get(compositionC1C2C3Key).getComposition());

            compositionRepository.removeFrom(nodeMap, C2.class);
            Assertions.assertNotNull(nodeMap);
            Assertions.assertEquals(1, nodeMap.size());
            Assertions.assertEquals(compositionC1, nodeMap.get(compositionC1Key).getComposition());
        }
    }

    record C1(int id) {
    }

    record C2(int id) {
    }

    record C3(int id) {
    }

    @Nested
    public class NodeTest {
        @Test
        void getOrCreateCompositions() {
            try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
                CompositionRepository.Node node = compositionRepository.new Node(C1.class, C2.class);
                Composition composition = node.getOrCreateComposition();
                Assertions.assertArrayEquals(new Class<?>[]{C1.class, C2.class}, composition.getComponentTypes());
            }
        }

        @Test
        void toStringTest() {
            try (CompositionRepository compositionRepository = new CompositionRepository(LoggingSystem.Context.TEST)) {
                CompositionRepository.Node node = compositionRepository.new Node(C1.class, C2.class);
                Assertions.assertEquals("Node={types=[C1,C2]}", node.toString());
            }
        }
    }
}
