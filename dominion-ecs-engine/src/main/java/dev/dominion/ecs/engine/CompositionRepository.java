package dev.dominion.ecs.engine;

import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.IntArraySort;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.HashCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public final class CompositionRepository implements AutoCloseable {

    private final ClassIndex classIndex = new ClassIndex();
    private final ConcurrentPool<LongEntity> pool = new ConcurrentPool<>();
    private final NodeCache nodeCache = new NodeCache();
    private final Node root;

    public CompositionRepository() {
        root = new Node();
        root.composition = new Composition(pool.newTenant(), classIndex);
    }

    public Composition getOrCreate(Object[] components) {
        switch (components.length) {
            case 0:
                return root.composition;
            case 1:
                Class<?> componentType = components[0].getClass();
                Node node = nodeCache.getNode(classIndex.getIndex(componentType));
                if (node == null) {
                    int key = classIndex.getIndexOrAddClass(componentType);
                    node = nodeCache.getNode(key);
                    if (node == null) {
                        node = nodeCache.getOrCreateNode(key, componentType);
                    }
                } else {
                    // node may not yet be connected to itself
                    node.linkNode(classIndex.getIndex(componentType), node);
                }
                return getNodeComposition(node);
            default:
                long hashCode = classIndex.longHashCode(components);
                node = nodeCache.getNode(hashCode);
                if (node == null) {
                    hashCode = HashCode.longHashCode(
                            IntArraySort.sort(classIndex.getIndexOrAddClassBatch(components)
                                    , classIndex.size() + 1)
                    );
                    node = nodeCache.getNode(hashCode);
                }
                if (node == null) {
                    node = nodeCache.getOrCreateNode(hashCode, getComponentTypes(components));
                }
                return getNodeComposition(node);
        }
    }

    private Class<?>[] getComponentTypes(Object[] components) {
        Class<?>[] componentTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            componentTypes[i] = components[i].getClass();
        }
        return componentTypes;
    }

    private Composition getNodeComposition(Node link) {
        Composition composition = link.getComposition();
        if (composition != null) {
            return composition;
        }
        return link.getOrCreateComposition();
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public Collection<Node> find(Class<?>... componentTypes) {
        switch (componentTypes.length) {
            case 0:
                return null;
            case 1:
                Node node = nodeCache.getNode(classIndex.getIndex(componentTypes[0]));
                return node == null ? null : node.linkedNodes.values();
            default:
                Map<Long, Node> currentCompositions = null;
                for (int i = 0; i < componentTypes.length; i++) {
                    node = nodeCache.getNode(classIndex.getIndex(componentTypes[i]));
                    if (node == null) {
                        continue;
                    }
                    currentCompositions = currentCompositions == null ?
                            node.copyLinkedNodeMap() :
                            retainAll(currentCompositions, node.getLinkedNodes())
                    ;
                }
                return currentCompositions == null ? null : currentCompositions.values();
        }
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    private Map<Long, Node> retainAll(Map<Long, Node> subject, Map<Long, Node> other) {
        Set<Long> longSet = subject.keySet();
        Iterator<Long> iterator = longSet.iterator();
        while (iterator.hasNext()) {
            if (!other.containsKey(iterator.next())) {
                iterator.remove();
            }
        }
        return subject;
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

        @SuppressWarnings("ForLoopReplaceableByForEach")
        public Node getOrCreateNode(long key, Class<?>... componentTypes) {
            Node node = data.computeIfAbsent(key, k -> new Node(componentTypes));
            if (componentTypes.length > 1) {
                for (int i = 0; i < componentTypes.length; i++) {
                    Class<?> componentType = componentTypes[i];
                    long typeKey = classIndex.getIndex(componentType);
                    Node singleTypeNode = data.computeIfAbsent(typeKey, k -> new Node(componentType));
                    singleTypeNode.linkNode(key, node);
                }
            } else {
                node.linkNode(key, node);
            }
            return node;
        }

        public Node getNode(long key) {
            return data.get(key);
        }

        public boolean contains(long key) {
            return data.containsKey(key);
        }
    }

    public final class Node {
        private final StampedLock lock = new StampedLock();
        private final Map<Long, Node> linkedNodes = new ConcurrentHashMap<>();
        private final Class<?>[] componentTypes;
        private Composition composition;

        public Node(Class<?>... componentTypes) {
            this.componentTypes = componentTypes;
        }

        public void linkNode(long key, Node node) {
            linkedNodes.putIfAbsent(key, node);
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
                    value = composition = new Composition(pool.newTenant(), classIndex, componentTypes);
                    break;
                }
                return value;
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
        }

        public Composition getComposition() {
            return composition;
        }

        public Map<Long, Node> getLinkedNodes() {
            return Collections.unmodifiableMap(linkedNodes);
        }

        public Map<Long, Node> copyLinkedNodeMap() {
            return new HashMap<>(linkedNodes);
        }

        @Override
        public String toString() {
            return componentTypes == null ?
                    "root" :
                    Arrays.stream(componentTypes)
                            .map(Class::getSimpleName)
                            .sorted()
                            .collect(Collectors.joining(","));
        }
    }
}
