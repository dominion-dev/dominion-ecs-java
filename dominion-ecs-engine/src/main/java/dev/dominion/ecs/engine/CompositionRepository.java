/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Composition;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.Config;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.Logging;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public final class CompositionRepository implements AutoCloseable {
    private static final System.Logger LOGGER = Logging.getLogger();
    private final NodeCache nodeCache = new NodeCache();
    private final ClassIndex classIndex;
    private final ChunkedPool<IntEntity> pool;
    private final IdSchema idSchema;
    private final PreparedComposition preparedComposition;
    private final Map<Class<?>, Composition.ByAdding1AndRemoving<?>> addingTypeModifiers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Composition.ByRemoving> removingTypeModifiers = new ConcurrentHashMap<>();
    private final Node root;
    private final Logging.Context loggingContext;

    public CompositionRepository(Logging.Context loggingContext) {
        this(Config.DominionSize.MEDIUM.classIndexBit()
                , Config.DominionSize.MEDIUM.chunkBit()
                , loggingContext
        );
    }

    public CompositionRepository(int classIndexBit, int chunkBit, Logging.Context loggingContext) {
        classIndex = new ClassIndex(classIndexBit, true, loggingContext);
        chunkBit = Math.max(IdSchema.MIN_CHUNK_BIT, Math.min(chunkBit, IdSchema.MAX_CHUNK_BIT));
        idSchema = new IdSchema(chunkBit);
        this.loggingContext = loggingContext;
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Creating " + getClass().getSimpleName()
                    )
            );
        }
        pool = new ChunkedPool<>(idSchema, loggingContext);
        preparedComposition = new PreparedComposition(this);
        root = new Node();
        root.composition = new DataComposition(this, pool, classIndex, idSchema, loggingContext);
    }

    public ChunkedPool<IntEntity> getPool() {
        return pool;
    }

    public IdSchema getIdSchema() {
        return idSchema;
    }

    public PreparedComposition getPreparedComposition() {
        return preparedComposition;
    }

    @SuppressWarnings("unchecked")
    public Composition.ByAdding1AndRemoving<Object> fetchAddingTypeModifier(Class<?> compType) {
        Composition.ByAdding1AndRemoving<Object> byAdding1AndRemoving = (Composition.ByAdding1AndRemoving<Object>) addingTypeModifiers.get(compType);
        return byAdding1AndRemoving == null ?
                (Composition.ByAdding1AndRemoving<Object>) addingTypeModifiers.computeIfAbsent(compType
                        , k -> preparedComposition.byAdding1AndRemoving(compType)) :
                byAdding1AndRemoving;
    }

    public Composition.ByRemoving fetchRemovingTypeModifier(Class<?> compType) {
        Composition.ByRemoving byRemoving = removingTypeModifiers.get(compType);
        return byRemoving == null ?
                removingTypeModifiers.computeIfAbsent(compType
                        , k -> preparedComposition.byRemoving(compType)) :
                byRemoving;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
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

    @SuppressWarnings("EnhancedSwitchMigration")
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

    @SuppressWarnings("resource")
    public void modifyComponents(IntEntity entity, PreparedComposition.TargetComposition targetComposition, Object addedComponent, Object[] addedComponents) {
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Modifying " + entity + " from " + entity.getComposition() + " to " + targetComposition.target())
            );
        }
        ChunkedPool.Tenant<IntEntity> prevTenant;
        synchronized (prevTenant = entity.getChunk().getTenant()) {
            int prevId = entity.getId();
            synchronized (targetComposition.target().getTenant()) {
                targetComposition.target().attachEntity(entity, targetComposition.indexMapping(), targetComposition.addedIndexMapping(), addedComponent, addedComponents);
            }
            prevTenant.freeId(prevId);
        }
        if (entity.stateChunk != null) {
            ChunkedPool.Tenant<IntEntity> prevStateTenant;
            synchronized (prevStateTenant = entity.stateChunk.getTenant()) {
                prevStateTenant.freeStateId(entity.getStateId());
                entity.stateChunk = targetComposition.target().fetchStateTenants((IndexKey) prevStateTenant.getSubject()).registerState(entity);
            }
        }
    }

    public Entity addComponent(IntEntity entity, Object component) {
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Adding [" + component.getClass().getSimpleName() + "] to " + entity)

            );
        }
        var modifier = fetchAddingTypeModifier(component.getClass());
        var mod = (PreparedComposition.NewEntityComposition) modifier.withValue(entity, component);
        modifyComponents(mod.entity(), mod.targetComposition(), mod.addedComponent(), mod.addedComponents());
        return entity;
    }

    public boolean removeComponentType(IntEntity entity, Class<?> componentType) {
        if (componentType == null) {
            return false;
        }
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Removing [" + componentType.getSimpleName() + "] from " + entity)
            );
        }
        var modifier = fetchRemovingTypeModifier(componentType);
        var mod = (PreparedComposition.NewEntityComposition) modifier.withValue(entity);
        if (mod == null) {
            return false;
        }
        modifyComponents(mod.entity(), mod.targetComposition(), mod.addedComponent(), mod.addedComponents());
        return true;
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach", "LocalVariableUsedAndDeclaredInDifferentSwitchBranches"})
    public Map<IndexKey, Node> findWith(Class<?>... componentTypes) {
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Find entities with " + Arrays.toString(componentTypes))
            );
        }
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
                        return null;
                    }
                    currentCompositions = currentCompositions == null ?
                            node.copyOfLinkedNodes() :
                            intersect(currentCompositions, node.linkedNodes)
                    ;
                }
                return currentCompositions;
        }
    }

    public void mapWithout(Map<IndexKey, Node> nodeMap, Class<?>... componentTypes) {
        if (nodeMap == null) {
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

    public void mapWithAlso(Map<IndexKey, Node> nodeMap, Class<?>... componentTypes) {
        if (nodeMap == null) {
            return;
        }
        for (Class<?> componentType : componentTypes) {
            Node node = nodeCache.getNode(new IndexKey(classIndex.getIndex(componentType)));
            if (node == null) {
                nodeMap.clear();
                return;
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

    public Logging.Context getLoggingContext() {
        return loggingContext;
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
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG
                        , Logging.format(loggingContext.subject(), "Creating " + this)
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
                    value = composition = new DataComposition(CompositionRepository.this, pool,
                            classIndex, idSchema, loggingContext, componentTypes);
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
