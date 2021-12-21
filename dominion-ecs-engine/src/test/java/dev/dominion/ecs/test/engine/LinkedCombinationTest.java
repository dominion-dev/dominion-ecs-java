package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.engine.LinkedCombinations;
import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class LinkedCombinationTest {

    @Test
    void createOrGetWith1Component() {
        try (LinkedCombinations linkedCombinations = new LinkedCombinations()) {
            linkedCombinations.createOrGet(C1.class);
            LinkedCombinations.Node root = linkedCombinations.getRoot();
            LinkedCombinations.Node link = root.getLink(C1.class);
            Assertions.assertNotNull(link);
            Assertions.assertTrue(link.hasComponentType(C1.class));
            Assertions.assertFalse(link.hasComponentType(C2.class));
        }
    }

    @Test
    void createOrGetWith2Component() {
        try (LinkedCombinations linkedCombinations = new LinkedCombinations()) {
            linkedCombinations.createOrGet(C1.class, C2.class);
            LinkedCombinations.Node root = linkedCombinations.getRoot();

            LinkedCombinations.Node c1Link = root.getLink(C1.class);
            Assertions.assertNotNull(c1Link);
            Assertions.assertTrue(c1Link.hasComponentType(C1.class));
            Assertions.assertFalse(c1Link.hasComponentType(C2.class));

            LinkedCombinations.Node c2Link = root.getLink(C2.class);
            Assertions.assertNotNull(c2Link);
            Assertions.assertTrue(c2Link.hasComponentType(C2.class));
            Assertions.assertFalse(c2Link.hasComponentType(C1.class));
        }
    }

    @Test
    void query() {
        // todo
    }

    private static class C1 implements Component {
    }

    private static class C2 implements Component {
    }

    @Nested
    public class NodeTest {

        @Test
        void createOrGetLink() {
            try (LinkedCombinations linkedArchetypes = new LinkedCombinations()) {
                LinkedCombinations.Node node = linkedArchetypes.new Node(null);
                node.createOrGetLink(C1.class);
                Assertions.assertTrue(linkedArchetypes.getClassIndex().getIndex(C1.class) > 0);
                Assertions.assertFalse(node.getLinks().isEmpty());
            }
        }

        @Test
        void getLink() {
            try (LinkedCombinations linkedArchetypes = new LinkedCombinations()) {
                LinkedCombinations.Node node = linkedArchetypes.new Node(null);
                Assertions.assertNull(node.getLink(C1.class));
                node.createOrGetLink(C1.class);
                Assertions.assertNotNull(node.getLink(C1.class));
            }
        }

        @Test
        void hasComponentType() {
            try (LinkedCombinations linkedArchetypes = new LinkedCombinations()) {
                LinkedCombinations.Node node = linkedArchetypes.new Node(null);
                Assertions.assertFalse(node.hasComponentType(C1.class));

                SparseIntMap<Class<? extends Component>> cTypes = new ConcurrentIntMap<>();
                cTypes.put(linkedArchetypes.getClassIndex().addClass(C2.class), C2.class);
                node = linkedArchetypes.new Node(cTypes);
                Assertions.assertFalse(node.hasComponentType(C1.class));
                Assertions.assertTrue(node.hasComponentType(C2.class));
            }
        }

        @Test
        void toStringTest() {
            try (LinkedCombinations linkedArchetypes = new LinkedCombinations()) {
                SparseIntMap<Class<? extends Component>> cTypes = new ConcurrentIntMap<>();
                cTypes.put(linkedArchetypes.getClassIndex().addClass(C1.class), C1.class);
                cTypes.put(linkedArchetypes.getClassIndex().addClass(C2.class), C2.class);
                LinkedCombinations.Node node = linkedArchetypes.new Node(cTypes);
                Assertions.assertEquals("C1,C2", node.toString());
            }
        }
    }
}
