package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.CompositionRepository;
import dev.dominion.ecs.engine.system.HashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class CompositionRepositoryTest {

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
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1()});
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
            Composition composition = compositionRepository.getOrCreate(new Object[]{new C1(), new C2()});
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
    void query() {
        try (CompositionRepository compositionRepository = new CompositionRepository()) {
            Composition compositionC1 = compositionRepository.getOrCreate(new Object[]{new C1()});
            Composition compositionC1C2 = compositionRepository.getOrCreate(new Object[]{new C1(), new C2()});
            Composition compositionC2C3 = compositionRepository.getOrCreate(new Object[]{new C2(), new C3()});

            Collection<CompositionRepository.Node> nodes = compositionRepository.query(C1.class);
            Assertions.assertNotNull(nodes);
            List<CompositionRepository.Node> nodeList = nodes.stream().toList();
            Assertions.assertEquals(2, nodeList.size());
            Assertions.assertEquals(compositionC1, nodeList.get(0).getComposition());
            Assertions.assertEquals(compositionC1C2, nodeList.get(1).getComposition());

            nodes = compositionRepository.query(C2.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(2, nodeList.size());
            Assertions.assertEquals(compositionC1C2, nodeList.get(0).getComposition());
            Assertions.assertEquals(compositionC2C3, nodeList.get(1).getComposition());

            nodes = compositionRepository.query(C3.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC2C3, nodeList.get(0).getComposition());

            nodes = compositionRepository.query(C2.class, C1.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC1C2, nodeList.get(0).getComposition());

            nodes = compositionRepository.query(C3.class, C2.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(1, nodeList.size());
            Assertions.assertEquals(compositionC2C3, nodeList.get(0).getComposition());

            nodes = compositionRepository.query(C3.class, C1.class);
            Assertions.assertNotNull(nodes);
            nodeList = nodes.stream().toList();
            Assertions.assertEquals(0, nodeList.size());
        }
    }

    private static class C1 {
    }

    private static class C2 {
    }

    private static class C3 {
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
