/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Composition;
import dev.dominion.ecs.api.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PreparedComposition implements Composition {

    private final CompositionRepository compositions;

    public PreparedComposition(CompositionRepository compositions) {
        this.compositions = compositions;
    }

    private static void populateIndexMapping(Class<?>[] componentTypes, int[] indexMapping, DataComposition context) {
        if (context.isMultiComponent()) {
            for (int i = 0; i < componentTypes.length; i++) {
                indexMapping[i] = context.fetchComponentIndex(componentTypes[i]);
            }
        } else {
            Class<?>[] newComponentTypes = context.getComponentTypes();
            Class<?> componentType = newComponentTypes.length > 0 ? newComponentTypes[0] : null;
            for (int i = 0; i < componentTypes.length; i++) {
                indexMapping[i] = componentTypes[i].equals(componentType) ? 0 : -1;
            }
        }
    }

    @Override
    public <T> Composition.Of1<T> of(Class<T> compType) {
        return new Of1<>(compositions.getOrCreateByType(new Class<?>[]{compType}));
    }

    @Override
    public <T1, T2> Of2<T1, T2> of(Class<T1> compType1, Class<T2> compType2) {
        Class<?>[] componentTypes = {compType1, compType2};
        return new Of2<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3> Of3<T1, T2, T3> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3) {
        Class<?>[] componentTypes = {compType1, compType2, compType3};
        return new Of3<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3, T4> Of4<T1, T2, T3, T4> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4) {
        Class<?>[] componentTypes = {compType1, compType2, compType3, compType4};
        return new Of4<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5> Of5<T1, T2, T3, T4, T5> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5) {
        Class<?>[] componentTypes = {compType1, compType2, compType3, compType4, compType5};
        return new Of5<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Of6<T1, T2, T3, T4, T5, T6> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6) {
        Class<?>[] componentTypes = {compType1, compType2, compType3, compType4, compType5, compType6};
        return new Of6<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7> Of7<T1, T2, T3, T4, T5, T6, T7> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7) {
        Class<?>[] componentTypes = {compType1, compType2, compType3, compType4, compType5, compType6, compType7};
        return new Of7<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7, T8> Of8<T1, T2, T3, T4, T5, T6, T7, T8> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7, Class<T8> compType8) {
        Class<?>[] componentTypes = {compType1, compType2, compType3, compType4, compType5, compType6, compType7, compType8};
        return new Of8<>(compositions.getOrCreateByType(componentTypes), componentTypes);
    }

    @Override
    public ByRemoving byRemoving(Class<?>... removedCompTypes) {
        return new PreparedModifier(compositions, null, removedCompTypes);
    }

    @Override
    public <T> ByAdding1AndRemoving<T> byAdding1AndRemoving(Class<T> compType, Class<?>... removedCompTypes) {
        return new ByAdding1AndRemoving<>(compositions, new Class<?>[]{compType}, removedCompTypes);
    }

    @Override
    public <T1, T2> ByAdding2AndRemoving<T1, T2> byAdding2AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<?>... removedCompTypes) {
        return new ByAdding2AndRemoving<>(compositions, new Class<?>[]{compType1, compType2}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3> ByAdding3AndRemoving<T1, T2, T3> byAdding3AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<?>... removedCompTypes) {
        return new ByAdding3AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3, T4> ByAdding4AndRemoving<T1, T2, T3, T4> byAdding4AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<?>... removedCompTypes) {
        return new ByAdding4AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3, compType4}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5> ByAdding5AndRemoving<T1, T2, T3, T4, T5> byAdding5AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<?>... removedCompTypes) {
        return new ByAdding5AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3, compType4, compType5}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> byAdding6AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<?>... removedCompTypes) {
        return new ByAdding6AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3, compType4, compType5, compType6}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7> ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> byAdding7AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7, Class<?>... removedCompTypes) {
        return new ByAdding7AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3, compType4, compType5, compType6, compType7}, removedCompTypes);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7, T8> ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> byAdding8AndRemoving(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7, Class<T8> compType8, Class<?>... removedCompTypes) {
        return new ByAdding8AndRemoving<>(compositions, new Class<?>[]{compType1, compType2, compType3, compType4, compType5, compType6, compType7, compType8}, removedCompTypes);
    }

    public static class OfTypes {
        protected final DataComposition context;
        protected final int[] indexMapping;

        protected Object[] components;

        public OfTypes(DataComposition context, Class<?>[] componentTypes) {
            this.context = context;
            if (componentTypes == null) {
                this.indexMapping = new int[0];
            } else {
                int length = componentTypes.length;
                this.indexMapping = new int[length];
                populateIndexMapping(componentTypes, indexMapping, context);
            }
        }

        public Object[] getComponents() {
            return components;
        }

        public Object getContext() {
            return context;
        }
    }

    public record TargetComposition(DataComposition target, int[] indexMapping, int[] addedIndexMapping) {
    }

    public record NewEntityComposition(IntEntity entity,
                                       TargetComposition targetComposition,
                                       Object addedComponent,
                                       Object[] addedComponents) implements Modifier {
    }

    public static class PreparedModifier implements ByRemoving {
        private final CompositionRepository compositions;
        private final Map<DataComposition, TargetComposition> cache = new ConcurrentHashMap<>();
        private final Class<?>[] addedComponentTypes;
        private final Set<Class<?>> removedComponentTypes;

        public PreparedModifier(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            this.compositions = compositions;
            this.addedComponentTypes = addedComponentTypes;
            removedComponentTypes = new HashSet<>(componentTypes.length);
            Collections.addAll(removedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity) {
            return fetchModifier(entity);
        }

        protected NewEntityComposition fetchModifier(Entity entity) {
            return fetchModifier(entity, (Object) null);
        }

        protected NewEntityComposition fetchModifier(Entity entity, Object addedComponent) {
            var intEntity = (IntEntity) entity;
            var composition = intEntity.getComposition();
            TargetComposition targetComposition = fetchTargetComposition(composition);
            return !targetComposition.target.equals(composition) ?
                    new NewEntityComposition(intEntity, targetComposition, addedComponent, null) :
                    null;
        }

        protected NewEntityComposition fetchModifier(Entity entity, Object... addedComponents) {
            var intEntity = (IntEntity) entity;
            var composition = intEntity.getComposition();
            TargetComposition targetComposition = fetchTargetComposition(composition);
            return !targetComposition.target.equals(composition) ?
                    new NewEntityComposition(intEntity, targetComposition, null, addedComponents) :
                    null;
        }

        private TargetComposition fetchTargetComposition(DataComposition composition) {
            TargetComposition targetComposition = cache.get(composition);
            return targetComposition == null ?
                    cache.computeIfAbsent(composition, this::getTargetComposition): targetComposition;
        }

        private TargetComposition getTargetComposition(DataComposition prevComposition) {
            Class<?>[] prevComponentTypes = prevComposition.getComponentTypes();
            int newLength = prevComponentTypes.length + (addedComponentTypes == null ? 0 : addedComponentTypes.length);
            List<Class<?>> typeList = new ArrayList<>(newLength);
            populateTypeList(typeList, prevComponentTypes);
            if (addedComponentTypes != null) {
                populateTypeList(typeList, addedComponentTypes);
            }
            Class<?>[] newComponentTypes = typeList.toArray(new Class<?>[0]);
            DataComposition newComposition = compositions.getOrCreateByType(newComponentTypes);
            int[] indexMapping = new int[prevComponentTypes.length];
            populateIndexMapping(prevComponentTypes, indexMapping, newComposition);
            int[] addedIndexMapping = null;
            if (addedComponentTypes != null) {
                addedIndexMapping = new int[addedComponentTypes.length];
                populateIndexMapping(addedComponentTypes, addedIndexMapping, newComposition);
            }
            return new TargetComposition(newComposition, indexMapping, addedIndexMapping);
        }

        private void populateTypeList(List<Class<?>> typeList, Class<?>[] componentTypes) {
            for (Class<?> type : componentTypes) {
                if (!removedComponentTypes.contains(type)) {
                    typeList.add(type);
                }
            }
        }
    }

    public final static class ByAdding1AndRemoving<T> extends PreparedModifier implements Composition.ByAdding1AndRemoving<T> {

        public ByAdding1AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T comp) {
            return fetchModifier(entity, comp);
        }
    }

    public final static class ByAdding2AndRemoving<T1, T2> extends PreparedModifier implements Composition.ByAdding2AndRemoving<T1, T2> {

        public ByAdding2AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2) {
            return fetchModifier(entity, comp1, comp2);
        }
    }

    public final static class ByAdding3AndRemoving<T1, T2, T3> extends PreparedModifier implements Composition.ByAdding3AndRemoving<T1, T2, T3> {

        public ByAdding3AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3) {
            return fetchModifier(entity, comp1, comp2, comp3);
        }
    }

    public final static class ByAdding4AndRemoving<T1, T2, T3, T4> extends PreparedModifier implements Composition.ByAdding4AndRemoving<T1, T2, T3, T4> {

        public ByAdding4AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4) {
            return fetchModifier(entity, comp1, comp2, comp3, comp4);
        }
    }

    public final static class ByAdding5AndRemoving<T1, T2, T3, T4, T5> extends PreparedModifier implements Composition.ByAdding5AndRemoving<T1, T2, T3, T4, T5> {

        public ByAdding5AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5) {
            return fetchModifier(entity, comp1, comp2, comp3, comp4, comp5);
        }
    }

    public final static class ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> extends PreparedModifier implements Composition.ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> {

        public ByAdding6AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6) {
            return fetchModifier(entity, comp1, comp2, comp3, comp4, comp5, comp6);
        }
    }

    public final static class ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> extends PreparedModifier implements Composition.ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> {

        public ByAdding7AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7) {
            return fetchModifier(entity, comp1, comp2, comp3, comp4, comp5, comp6, comp7);
        }
    }

    public final static class ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> extends PreparedModifier implements Composition.ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> {

        public ByAdding8AndRemoving(CompositionRepository compositions, Class<?>[] addedComponentTypes, Class<?>... componentTypes) {
            super(compositions, addedComponentTypes, componentTypes);
        }

        @Override
        public Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7, T8 comp8) {
            return fetchModifier(entity, comp1, comp2, comp3, comp4, comp5, comp6, comp7, comp8);
        }
    }

    public final static class Of1<T> extends OfTypes implements Composition.Of1<T> {

        public Of1(DataComposition context) {
            super(context, null);
        }

        @Override
        public Composition.OfTypes withValue(T comp) {
            components = new Object[]{comp};
            return this;
        }
    }

    public final static class Of2<T1, T2> extends OfTypes implements Composition.Of2<T1, T2> {

        public Of2(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2) {
            components = new Object[2];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            return this;
        }
    }

    public final static class Of3<T1, T2, T3> extends OfTypes implements Composition.Of3<T1, T2, T3> {

        public Of3(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3) {
            components = new Object[3];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            return this;
        }
    }

    public final static class Of4<T1, T2, T3, T4> extends OfTypes implements Composition.Of4<T1, T2, T3, T4> {

        public Of4(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4) {
            components = new Object[4];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            components[indexMapping[3]] = comp4;
            return this;
        }
    }

    public final static class Of5<T1, T2, T3, T4, T5> extends OfTypes implements Composition.Of5<T1, T2, T3, T4, T5> {

        public Of5(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5) {
            components = new Object[5];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            components[indexMapping[3]] = comp4;
            components[indexMapping[4]] = comp5;
            return this;
        }
    }

    public final static class Of6<T1, T2, T3, T4, T5, T6> extends OfTypes implements Composition.Of6<T1, T2, T3, T4, T5, T6> {

        public Of6(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6) {
            components = new Object[6];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            components[indexMapping[3]] = comp4;
            components[indexMapping[4]] = comp5;
            components[indexMapping[5]] = comp6;
            return this;
        }
    }

    public final static class Of7<T1, T2, T3, T4, T5, T6, T7> extends OfTypes implements Composition.Of7<T1, T2, T3, T4, T5, T6, T7> {

        public Of7(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7) {
            components = new Object[7];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            components[indexMapping[3]] = comp4;
            components[indexMapping[4]] = comp5;
            components[indexMapping[5]] = comp6;
            components[indexMapping[6]] = comp7;
            return this;
        }
    }

    public final static class Of8<T1, T2, T3, T4, T5, T6, T7, T8> extends OfTypes implements Composition.Of8<T1, T2, T3, T4, T5, T6, T7, T8> {

        public Of8(DataComposition context, Class<?>[] componentTypes) {
            super(context, componentTypes);
        }

        @Override
        public Composition.OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7, T8 comp8) {
            components = new Object[8];
            components[indexMapping[0]] = comp1;
            components[indexMapping[1]] = comp2;
            components[indexMapping[2]] = comp3;
            components[indexMapping[3]] = comp4;
            components[indexMapping[4]] = comp5;
            components[indexMapping[5]] = comp6;
            components[indexMapping[6]] = comp7;
            components[indexMapping[7]] = comp8;
            return this;
        }
    }
}
