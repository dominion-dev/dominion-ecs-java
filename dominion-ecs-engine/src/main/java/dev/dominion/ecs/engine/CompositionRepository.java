/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Composition;
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
    private final PreparedComposition preparedComposition;
    private final Map<Class<?>, Composition.ByAdding1AndRemoving<?>> addingTypeModifiers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Composition.ByRemoving> removingTypeModifiers = new ConcurrentHashMap<>();
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
        preparedComposition = new PreparedComposition(this);
        root = new Node();
        arrayPool = new ObjectArrayPool(loggingContext);
        root.composition = new DataComposition(this, pool.newTenant(), arrayPool, classIndex, idSchema, loggingContext);
    }

    public IdSchema getIdSchema() {
        return idSchema;
    }

    public PreparedComposition getPreparedComposition() {
        return preparedComposition;
    }

    @SuppressWarnings("unchecked")
    public Composition.ByAdding1AndRemoving<Object> fetchAddingTypeModifier(Class<?> compType) {
        return (Composition.ByAdding1AndRemoving<Object>) addingTypeModifiers.computeIfAbsent(compType
                , k -> preparedComposition.byAdding1AndRemoving(compType));
    }

    public Composition.ByRemoving fetchRemovingTypeModifier(Class<?> compType) {
        return removingTypeModifiers.computeIfAbsent(compType
                , k -> preparedComposition.byRemoving(compType));
    }

    public DataComposition getOrCreate(Object[] components) {
        int componentsLength = components == null ? 0 : components.length;
        switch (componentsLength) {
            case 0:
                return root.composition;
            case 1:
                return getSingleTypeComposition(components[0].getClass());
            default:
                IndexKey indexKey = classIndex.getIndexKey(components);
                Node node = nodeCache.getNode(indexKey);
                if (node == null) {
                    node = nodeCache.getOrCreateNode(indexKey, getComponentTypes(components));
                }
                return getNodeComposition(node);
        }
    }

    public DataComposition getOrCreateByType(Class<?>[] componentTypes) {
        int length = componentTypes == null ? 0 : componentTypes.length;
        switch (length) {
            case 0:
                return root.composition;
            case 1:
                return getSingleTypeComposition(componentTypes[0]);
            default:
                IndexKey indexKey = classIndex.getIndexKeyByType(componentTypes);
                Node node = nodeCache.getNode(indexKey);
                if (node == null) {
                    node = nodeCache.getOrCreateNode(indexKey, componentTypes);
                }
                return getNodeComposition(node);
        }
    }

    private DataComposition getSingleTypeComposition(Class<?> componentType) {
        IndexKey key = new IndexKey(classIndex.getIndex(componentType));
        Node node = nodeCache.getNode(key);
        if (node == null) {
            key = new IndexKey(classIndex.getIndexOrAddClass(componentType));
            node = nodeCache.getNode(key);
            if (node == null) {
                node = nodeCache.getOrCreateNode(key, componentType);
            }
        } else {
            // node may not be yet connected to itself
            node.linkNode(new IndexKey(classIndex.getIndex(componentType)), node);
        }
        return getNodeComposition(node);
    }

    private Class<?>[] getComponentTypes(Object[] components) {
        Class<?>[] componentTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            componentTypes[i] = components[i].getClass();
        }
        return componentTypes;
    }

    private DataComposition getNodeComposition(Node link) {
        DataComposition composition = link.getComposition();
        if (composition != null) {
            return composition;
        }
        return link.getOrCreateComposition();
    }

    public void modifyComponents(IntEntity entity, DataComposition newDataComposition, Object[] newComponentArray) {
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Modifying " + entity + " from " + entity.getComposition() + " to " + newDataComposition)

            );
        }
        entity.getComposition().detachEntity(entity);
        newDataComposition.attachEntity(entity, true, newComponentArray);
    }

    public Entity addComponent(IntEntity entity, Object component) {
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Adding [" + component.getClass().getSimpleName() + "] to " + entity)

            );
        }
        var modifier = fetchAddingTypeModifier(component.getClass());
        var mod = (PreparedComposition.NewEntityComposition) modifier.withValue(entity, component).getModifier();
        modifyComponents(mod.entity(), mod.newDataComposition(), mod.newComponentArray());
        return entity;
    }

    public boolean removeComponentType(IntEntity entity, Class<?> componentType) {
        if (componentType == null) {
            return false;
        }
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Removing [" + componentType.getSimpleName() + "] from " + entity)
            );
        }
        var modifier = fetchRemovingTypeModifier(componentType);
        var mod = (PreparedComposition.NewEntityComposition) modifier.withValue(entity).getModifier();
        if(mod == null) {
            return false;
        }
        modifyComponents(mod.entity(), mod.newDataComposition(), mod.newComponentArray());
        return true;
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

    public void without(Map<IndexKey, Node> nodeMap, Class<?>... componentTypes) {
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

    public void withAlso(Map<IndexKey, Node> nodeMap, Class<?>... componentTypes) {
        if (componentTypes.length == 0) {
            return;
        }
        for (Class<?> componentType : componentTypes) {
            Node node = nodeCache.getNode(new IndexKey(classIndex.getIndex(componentType)));
            if (node == null) {
                continue;
            }
            intersect(nodeMap, node.linkedNodes);
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
        private DataComposition composition;

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

        public DataComposition getOrCreateComposition() {
            DataComposition value;
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
                    value = composition = new DataComposition(CompositionRepository.this, pool.newTenant()
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

        public DataComposition getComposition() {
            return composition;
        }

        public Map<IndexKey, Node> getLinkedNodes() {
            return Collections.unmodifiableMap(linkedNodes);
        }

        public Map<IndexKey, Node> copyOfLinkedNodes() {
            return new ConcurrentHashMap<>(linkedNodes);
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
                    + "]}";
        }
    }
}
