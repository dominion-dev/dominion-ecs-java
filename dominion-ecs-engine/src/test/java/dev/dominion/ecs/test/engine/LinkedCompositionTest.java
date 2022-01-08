package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.Composition;
import dev.dominion.ecs.engine.LinkedCompositions;
import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import dev.dominion.ecs.engine.system.HashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class LinkedCompositionTest {

    @Test
    void getOrCreateWith1Component() {
        try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
            Composition composition = linkedCompositions.getOrCreate(C1.class);
            Assertions.assertArrayEquals(new Class<?>[]{C1.class}, composition.getComponentTypes());
            LinkedCompositions.Node root = linkedCompositions.getRoot();
            LinkedCompositions.Node link = root.getLink(C1.class);
            Assertions.assertNotNull(link);
            Assertions.assertTrue(link.hasComponentType(C1.class));
            Assertions.assertFalse(link.hasComponentType(C2.class));

            Assertions.assertTrue(linkedCompositions.getNodeCache()
                    .contains(HashCode.longHashCode(new int[]{
                            linkedCompositions.getClassIndex().getIndex(C1.class)
                    })));
        }
    }

    @Test
    void getOrCreateWith2Component() {
        try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
            Composition composition = linkedCompositions.getOrCreate(C1.class, C2.class);
            Assertions.assertArrayEquals(new Class<?>[]{C1.class, C2.class}, composition.getComponentTypes());

            LinkedCompositions.Node root = linkedCompositions.getRoot();
            LinkedCompositions.Node c1Link = root.getLink(C1.class);
            Assertions.assertNotNull(c1Link);
            Assertions.assertTrue(c1Link.hasComponentType(C1.class));
            Assertions.assertFalse(c1Link.hasComponentType(C2.class));
            Assertions.assertNull(c1Link.getComposition());

            LinkedCompositions.Node c2Link = root.getLink(C2.class);
            Assertions.assertNotNull(c2Link);
            Assertions.assertTrue(c2Link.hasComponentType(C2.class));
            Assertions.assertFalse(c2Link.hasComponentType(C1.class));
            Assertions.assertNull(c2Link.getComposition());

            LinkedCompositions.Node c1c2Link = c1Link.getLink(C2.class);
            Assertions.assertNotNull(c1c2Link);
            Assertions.assertTrue(c1c2Link.hasComponentType(C2.class));
            Assertions.assertTrue(c1c2Link.hasComponentType(C1.class));
            Assertions.assertNotNull(c1c2Link.getComposition());

            Assertions.assertTrue(linkedCompositions.getNodeCache()
                    .contains(HashCode.longHashCode(new int[]{
                            linkedCompositions.getClassIndex().getIndex(C1.class)
                            , linkedCompositions.getClassIndex().getIndex(C2.class)
                    })));
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
        void getOrCreateLink() {
            try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
                LinkedCompositions.Node node = linkedCompositions.new Node(null);
                node.getOrCreateLink(C1.class);
                Assertions.assertTrue(linkedCompositions.getClassIndex().getIndex(C1.class) > 0);
                Assertions.assertFalse(node.getLinks().isEmpty());
            }
        }

        @Test
        void getLink() {
            try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
                LinkedCompositions.Node node = linkedCompositions.new Node(null);
                Assertions.assertNull(node.getLink(C1.class));
                node.getOrCreateLink(C1.class);
                Assertions.assertNotNull(node.getLink(C1.class));
            }
        }

        @Test
        void hasComponentType() {
            try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
                LinkedCompositions.Node node = linkedCompositions.new Node(null);
                Assertions.assertFalse(node.hasComponentType(C1.class));

                SparseIntMap<Class<?>> cTypes = new ConcurrentIntMap<>();
                cTypes.put(linkedCompositions.getClassIndex().addClass(C2.class), C2.class);
                node = linkedCompositions.new Node(cTypes);
                Assertions.assertFalse(node.hasComponentType(C1.class));
                Assertions.assertTrue(node.hasComponentType(C2.class));
            }
        }

        @Test
        void toStringTest() {
            try (LinkedCompositions linkedCompositions = new LinkedCompositions()) {
                SparseIntMap<Class<?>> cTypes = new ConcurrentIntMap<>();
                cTypes.put(linkedCompositions.getClassIndex().addClass(C1.class), C1.class);
                cTypes.put(linkedCompositions.getClassIndex().addClass(C2.class), C2.class);
                LinkedCompositions.Node node = linkedCompositions.new Node(cTypes);
                Assertions.assertEquals("C1,C2", node.toString());
            }
        }
    }
}
