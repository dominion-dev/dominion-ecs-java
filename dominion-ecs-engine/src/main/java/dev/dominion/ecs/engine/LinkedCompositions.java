package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Component;
import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import dev.dominion.ecs.engine.system.ClassIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LinkedCompositions implements AutoCloseable {

    private final ClassIndex classIndex = new ClassIndex();
    private final ConcurrentPool<LongEntity> pool = new ConcurrentPool<>();
    private final NodeCache nodeCache = new NodeCache();
    private final Node root;

    public LinkedCompositions() {
        root = new Node(null);
        root.composition = new Composition(pool.newTenant());
    }

    @SafeVarargs
    public final Composition getOrCreate(Class<? extends Component>... componentTypes) {
        switch (componentTypes.length) {
            case 0:
                return root.composition;
            case 1:
                Node link = root.getLink(classIndex.getIndexOrAddClass(componentTypes[0]));
                if (link == null) {
                    link = root.getOrCreateLink(componentTypes[0]);
                }
                return link.getOrCreateComposition();
            default:
                long hashCode = NodeCache.longHashCode(
                        Arrays.stream(componentTypes).map(classIndex::getIndexOrAddClass)
                );
                link = nodeCache.getNode(hashCode);
                if (link == null) {
                    traverseNode(root, componentTypes);
                    link = nodeCache.getNode(hashCode);
                }
                return link.getOrCreateComposition();
        }
    }

    private void traverseNode(Node currentNode, Class<? extends Component>[] componentTypes) {
        for (Class<? extends Component> componentType : componentTypes) {
            if (currentNode.hasComponentType(componentType)) continue;
            Node node = currentNode.getOrCreateLink(componentType);
            traverseNode(node, componentTypes);
        }
    }

    @SafeVarargs
    public final Set<Composition> query(Class<? extends Component>... componentTypes) {
        return null;
    }

    public ClassIndex getClassIndex() {
        return classIndex;
    }

    public Node getRoot() {
        return root;
    }

    public NodeCache getNodeCache() {
        return nodeCache;
    }

    @Override
    public void close() {
        classIndex.close();
        pool.close();
    }

    public final class NodeCache {
        private final Map<Long, Node> data = new ConcurrentHashMap<>();

        public static long longHashCode(Stream<Integer> ints) {
            return ints
                    .sorted()
                    .reduce(0, (sum, hashCode) -> 31 * sum + hashCode);
        }

        public Node getNode(long key) {
            return data.get(key);
        }

        public Node getOrCreateNode(SparseIntMap<Class<? extends Component>> componentTypes) {
            long key = longHashCode(componentTypes.keysStream());
            return data.computeIfAbsent(key, k -> new Node(componentTypes));
        }

        public boolean contains(long key) {
            return data.containsKey(key);
        }
    }

    public final class Node {
        private final SparseIntMap<Class<? extends Component>> componentTypes;
        private final SparseIntMap<Node> links = new ConcurrentIntMap<>();
        private final StampedLock lock = new StampedLock();
        private Composition composition;

        public Node(SparseIntMap<Class<? extends Component>> componentTypes) {
            this.componentTypes = componentTypes;
        }

        public Node getOrCreateLink(Class<? extends Component> componentType) {
            int key = classIndex.getIndexOrAddClass(componentType);
            return links.computeIfAbsent(key,
                    k -> {
                        var cTypes = componentTypes == null ?
                                new ConcurrentIntMap<Class<? extends Component>>() :
                                componentTypes.clone();
                        cTypes.put(k, componentType);
                        return nodeCache.getOrCreateNode(cTypes);
                    });
        }

        public Node getLink(int key) {
            if (key == 0) return null;
            return links.get(key);
        }

        public Node getLink(Class<? extends Component> componentType) {
            return getLink(classIndex.getIndex(componentType));
        }

        public SparseIntMap<Node> getLinks() {
            return SparseIntMap.UnmodifiableView.wrap(links);
        }

        public Composition getOrCreateComposition() {
            Composition value;
            long stamp = lock.tryOptimisticRead();
            try {
                for (; ; stamp = lock.writeLock()) {
                    if (stamp == 0L)
                        continue;
                    // possibly racy reads
                    value = composition;
                    if (!lock.validate(stamp))
                        continue;
                    if (value != null)
                        break;
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0L)
                        continue;
                    // exclusive access
                    value = composition = new Composition(pool.newTenant(), componentTypes.values());
                    break;
                }
                return value;
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
        }

        @Override
        public String toString() {
            return componentTypes == null ?
                    "root" :
                    componentTypes.stream()
                            .map(Class::getSimpleName)
                            .sorted()
                            .collect(Collectors.joining(","));
        }

        public boolean hasComponentType(Class<? extends Component> componentType) {
            return componentTypes != null
                    && componentTypes.contains(classIndex.getIndex(componentType));
        }
    }
}
