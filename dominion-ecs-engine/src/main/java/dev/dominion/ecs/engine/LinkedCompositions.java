package dev.dominion.ecs.engine;

import dev.dominion.ecs.engine.collections.ConcurrentIntMap;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.IntArraySort;
import dev.dominion.ecs.engine.collections.SparseIntMap;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.HashCode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public final class LinkedCompositions implements AutoCloseable {

    private final ClassIndex classIndex = new ClassIndex();
    private final ConcurrentPool<LongEntity> pool = new ConcurrentPool<>();
    private final NodeCache nodeCache = new NodeCache();
    private final Node root;

    public LinkedCompositions() {
        root = new Node(null);
        root.composition = new Composition(pool.newTenant(), classIndex);
    }

    private static Class<?>[] getClasses(Object[] components) {
        Class<?>[] classes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            classes[i] = components[i].getClass();
        }
        return classes;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private SparseIntMap<Class<?>> getComponentTypes(Object[] components) {
        SparseIntMap<Class<?>> componentTypes = new ConcurrentIntMap<>();
        for (int i = 0; i < components.length; i++) {
            Class<?> klass = components[i].getClass();
            componentTypes.put(classIndex.getIndexOrAddClass(klass), klass);
        }
        return componentTypes;
    }

    public Composition getOrCreate(Object[] components) {
        switch (components.length) {
            case 0:
                return root.composition;
            case 1:
                Class<?> componentType = components[0].getClass();
                Node link = root.getLink(classIndex.getIndex(componentType));
                if (link == null) {
                    link = root.getLink(classIndex.getIndexOrAddClass(componentType));
                    if (link == null) {
                        link = root.getOrCreateLink(componentType);
                    }
                }
                return getLinkComposition(link);
            default:
                long hashCode = classIndex.longHashCode(components);
                link = nodeCache.getNode(hashCode);
                if (link == null) {
                    hashCode = HashCode.longHashCode(
                            IntArraySort.sort(classIndex.getIndexOrAddClassBatch(components)
                                    , classIndex.size() + 1)
                    );
                    link = nodeCache.getNode(hashCode);
                }
                if (link == null) {
//                    traverseNode(root, getClasses(components), 0);
//                    link = nodeCache.getNode(hashCode);
                    link = nodeCache.getOrCreateNode(getComponentTypes(components));
                }
                return getLinkComposition(link);
        }
    }

    private Composition getLinkComposition(Node link) {
        Composition composition = link.getComposition();
        if (composition != null) {
            return composition;
        }
        return link.getOrCreateComposition();
    }

    private void traverseNode(Node currentNode, Class<?>[] componentTypes, int start) {
        for (int i = start; i < componentTypes.length; i++) {
            Class<?> componentType = componentTypes[i];
            if (currentNode.hasComponentType(componentType)) continue;
            Node node = currentNode.getOrCreateLink(componentType);
            int next = i + 1;
            if (next < componentTypes.length) {
                traverseNode(node, componentTypes, next);
            }
        }
    }

    public Set<Composition> query(Class<?>... componentTypes) {
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

        public Node getNode(long key) {
            return data.get(key);
        }

        public Node getOrCreateNode(SparseIntMap<Class<?>> componentTypes) {
            long key = componentTypes.sortedKeysHashCode();
            return data.computeIfAbsent(key, k -> new Node(componentTypes));
        }

        public boolean contains(long key) {
            return data.containsKey(key);
        }
    }

    public final class Node {
        private final SparseIntMap<Class<?>> componentTypes;
        private final SparseIntMap<Node> links = new ConcurrentIntMap<>();
        private final StampedLock lock = new StampedLock();
        private Composition composition;

        public Node(SparseIntMap<Class<?>> componentTypes) {
            this.componentTypes = componentTypes;
        }

        public Node getOrCreateLink(Class<?> componentType) {
            int key = classIndex.getIndexOrAddClass(componentType);
            return links.computeIfAbsent(key,
                    k -> {
                        var cTypes = componentTypes == null ?
                                new ConcurrentIntMap<Class<?>>() :
                                componentTypes.clone();
                        cTypes.put(k, componentType);
                        return nodeCache.getOrCreateNode(cTypes);
                    });
        }

        public Node getLink(int key) {
            if (key == 0) return null;
            return links.get(key);
        }

        public Node getLink(Class<?> componentType) {
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
                    value = composition = new Composition(pool.newTenant(), classIndex, componentTypes.values());
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

        public boolean hasComponentType(Class<?> componentType) {
            return componentTypes != null
                    && componentTypes.contains(classIndex.getIndex(componentType));
        }

        public Composition getComposition() {
            return composition;
        }
    }
}
