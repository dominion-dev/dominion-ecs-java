/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Composition;

public class PreparedComposition implements Composition {

    private final CompositionRepository compositions;

    public PreparedComposition(CompositionRepository compositions) {
        this.compositions = compositions;
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
                for (int i = 0; i < length; i++) {
                    indexMapping[i] = context.fetchComponentIndex(componentTypes[i]);
                }
            }
        }

        public Object[] getComponents() {
            return components;
        }

        public Object getContext() {
            return context;
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
