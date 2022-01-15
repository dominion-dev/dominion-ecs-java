package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.CompositionRepository;
import dev.dominion.ecs.engine.system.HashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        // todo
    }

    private static class C1 {
    }

    private static class C2 {
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
