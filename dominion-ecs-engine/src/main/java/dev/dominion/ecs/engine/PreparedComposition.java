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
}
