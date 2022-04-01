/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.collections.ObjectArrayPool;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public final class CompositionRepository implements AutoCloseable {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final ObjectArrayPool arrayPool;
    private final NodeCache nodeCache = new NodeCache();
    private final ClassIndex classIndex;
    private final ChunkedPool<IntEntity> pool;
    private final IdSchema idSchema;
    private final Node root;
    private final LoggingSystem.Context loggingContext;

    public CompositionRepository(LoggingSystem.Context loggingContext) {
        this(ConfigSystem.DEFAULT_CLASS_INDEX_BIT
                , ConfigSystem.DEFAULT_CHUNK_BIT
                , ConfigSystem.DEFAULT_CHUNK_COUNT_BIT
                , loggingContext
        );
    }

    public CompositionRepository(
            int classIndexBit, int chunkBit, int chunkCountBit
            , LoggingSystem.Context loggingContext
    ) {
        classIndex = new ClassIndex(classIndexBit, true, loggingContext);
        chunkBit = Math.max(IdSchema.MIN_CHUNK_BIT, Math.min(chunkBit, IdSchema.MAX_CHUNK_BIT));
        int reservedBit = IdSchema.BIT_LENGTH - chunkBit;
        chunkCountBit = Math.max(IdSchema.MIN_CHUNK_COUNT_BIT,
                Math.min(chunkCountBit, Math.min(reservedBit, IdSchema.MAX_CHUNK_COUNT_BIT)));
        idSchema = new IdSchema(chunkBit, chunkCountBit);
        this.loggingContext = loggingContext;
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + getClass().getSimpleName()
                    )
            );
        }
        pool = new ChunkedPool<>(idSchema, loggingContext);
        root = new Node();
        arrayPool = new ObjectArrayPool(loggingContext);
        root.composition = new Composition(this, pool.newTenant(), arrayPool, classIndex, idSchema, loggingContext);
    }

    public IdSchema getIdSchema() {
        return idSchema;
    }

    public Composition getOrCreate(Object[] components) {
        int componentsLength = components == null ? 0 : components.length;
        switch (componentsLength) {
            case 0:
                return root.composition;
            case 1:
                Class<?> componentType = components[0].getClass();
                Node node = nodeCache.getNode(new IndexKey(classIndex.getIndex(componentType)));
                if (node == null) {
                    IndexKey key = new IndexKey(classIndex.getIndexOrAddClass(componentType));
                    node = nodeCache.getNode(key);
                    if (node == null) {
                        node = nodeCache.getOrCreateNode(key, componentType);
                    }
                } else {
                    // node may not yet be connected to itself
                    node.linkNode(new IndexKey(classIndex.getIndex(componentType)), node);
                }
                return getNodeComposition(node);
            default:
                IndexKey indexKey = classIndex.getIndexKey(components);
                node = nodeCache.getNode(indexKey);
                if (node == null) {
                    node = nodeCache.getOrCreateNode(indexKey, getComponentTypes(components));
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


    public Entity addComponents(IntEntity entity, Object... components) {
        if (components.length == 0) {
            return entity;
        }
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Adding [" + Arrays.stream(components).map(o -> o.getClass().getSimpleName())
                                    .collect(Collectors.joining(",")) + "] to " + entity)

            );
        }
        int componentsLength = components.length;
        Composition prevComposition = entity.getComposition();
        Object[] entityComponents = entity.getComponents();
        int prevComponentsLength = prevComposition.length();
        if (prevComponentsLength == 0) {
            Composition composition = getOrCreate(components);
            return composition.attachEntity(prevComposition.detachEntity(entity), components);
        }
        Object[] newComponentArray = arrayPool.pop(prevComponentsLength + componentsLength);
        if (prevComponentsLength == 1) {
            newComponentArray[0] = entityComponents[0];
        } else {
            System.arraycopy(entityComponents, 0, newComponentArray, 0, prevComponentsLength);
        }
        if (componentsLength == 1) {
            newComponentArray[prevComponentsLength] = components[0];
        } else {
            System.arraycopy(components, 0, newComponentArray, prevComponentsLength, componentsLength);
        }
        Composition composition = getOrCreate(newComponentArray);
        prevComposition.detachEntity(entity);
        if (entity.isPooledArray()) {
            arrayPool.push(entityComponents);
        }
        entity.flagPooledArray();
        return composition.attachEntity(entity, newComponentArray);
    }


    public Object removeComponentType(IntEntity entity, Class<?> componentType) {
        if (componentType == null) {
            return null;
        }
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Removing [" + componentType.getSimpleName() + "] from " + entity)
            );
        }
        Composition prevComposition = entity.getComposition();
        Object[] entityComponents = entity.getComponents();
        int prevComponentsLength = prevComposition.length();
        if (prevComponentsLength == 0) {
            return null;
        }
        Object[] newComponentArray;
        Object removed;
        if (prevComponentsLength == 1) {
            newComponentArray = null;
            removed = entityComponents[0];
        } else {
            newComponentArray = arrayPool.pop(prevComponentsLength - 1);
            int removedIndex = prevComposition.fetchComponentIndex(componentType);
            removed = entityComponents[removedIndex];
            if (removedIndex > 0) {
                System.arraycopy(entityComponents, 0, newComponentArray, 0, removedIndex);
            }
            if (removedIndex < prevComponentsLength - 1) {
                System.arraycopy(entityComponents, removedIndex + 1, newComponentArray, removedIndex, prevComponentsLength - (removedIndex + 1));
            }
        }
        Composition composition = getOrCreate(newComponentArray);
        prevComposition.detachEntity(entity);
        if (entity.isPooledArray()) {
            arrayPool.push(entityComponents);
        }
        entity.flagPooledArray();
        composition.attachEntity(entity, newComponentArray);
        return removed;
    }


    @SuppressWarnings("ForLoopReplaceableByForEach")
    public Map<IndexKey, Node> findWith(Class<?>... componentTypes) {
        switch (componentTypes.length) {
            case 0:
                return null;
            case 1:
                Node node = nodeCache.getNode(new IndexKey(classIndex.getIndex(componentTypes[0])));
                return node == null ? null : node.copyOfLinkedNodes();
            default:
                Map<IndexKey, Node> currentCompositions = null;
                for (int i = 0; i < componentTypes.length; i++) {
                    node = nodeCache.getNode(new IndexKey(classIndex.getIndex(componentTypes[i])));
                    if (node == null) {
                        continue;
                    }
                    currentCompositions = currentCompositions == null ?
                            node.copyOfLinkedNodes() :
                            intersect(currentCompositions, node.linkedNodes)
                    ;
                }
                return currentCompositions;
        }
    }

    public void removeFrom(Map<IndexKey, Node> nodeMap, Class<?>... componentTypes) {
        if (componentTypes.length == 0) {
            return;
        }
        for (Class<?> componentType : componentTypes) {
            IndexKey indexKey = new IndexKey(classIndex.getIndex(componentType));
            nodeMap.remove(indexKey);
            Node node = nodeCache.getNode(indexKey);
            if (node != null) {
                for (IndexKey linkedNodeKey : node.linkedNodes.keySet()) {
                    nodeMap.remove(linkedNodeKey);
                }
            }
        }
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    private Map<IndexKey, Node> intersect(Map<IndexKey, Node> subject, Map<IndexKey, Node> other) {
        Set<IndexKey> indexKeySet = subject.keySet();
        Iterator<IndexKey> iterator = indexKeySet.iterator();
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
        nodeCache.clear();
        classIndex.close();
        pool.close();
    }

    public final class NodeCache {
        private final Map<IndexKey, Node> data = new ConcurrentHashMap<>();

        @SuppressWarnings("ForLoopReplaceableByForEach")
        public Node getOrCreateNode(IndexKey key, Class<?>... componentTypes) {
            Node node = data.computeIfAbsent(key, k -> new Node(componentTypes));
            if (componentTypes.length > 1) {
                for (int i = 0; i < componentTypes.length; i++) {
                    Class<?> componentType = componentTypes[i];
                    IndexKey typeKey = new IndexKey(classIndex.getIndex(componentType));
                    Node singleTypeNode = data.computeIfAbsent(typeKey, k -> new Node(componentType));
                    singleTypeNode.linkNode(key, node);
                }
            } else {
                node.linkNode(key, node);
            }

            return node;
        }

        public Node getNode(IndexKey key) {
            return data.get(key);
        }

        public boolean contains(IndexKey key) {
            return data.containsKey(key);
        }

        public void clear() {
            data.clear();
        }
    }

    public final class Node {
        private final StampedLock lock = new StampedLock();
        private final Map<IndexKey, Node> linkedNodes = new ConcurrentHashMap<>();
        private final Class<?>[] componentTypes;
        private Composition composition;

        public Node(Class<?>... componentTypes) {
            this.componentTypes = componentTypes;
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG
                        , LoggingSystem.format(loggingContext.subject(), "Creating " + this)
                );
            }
        }

        public void linkNode(IndexKey key, Node node) {
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
                    value = composition = new Composition(CompositionRepository.this, pool.newTenant()
                            , arrayPool, classIndex, idSchema, loggingContext, componentTypes);
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

        public Map<IndexKey, Node> getLinkedNodes() {
            return Collections.unmodifiableMap(linkedNodes);
        }

        public Map<IndexKey, Node> copyOfLinkedNodes() {
            return new HashMap<>(linkedNodes);
        }

        @Override
        public String toString() {
            return "Node={"
                    + "types=[" + (componentTypes == null ?
                    "" :
                    Arrays.stream(componentTypes)
                            .map(Class::getSimpleName)
                            .sorted()
                            .collect(Collectors.joining(",")))
                    + "]"
//                    + ", links=" + linkedNodes
                    + "}";
        }

    }
}
