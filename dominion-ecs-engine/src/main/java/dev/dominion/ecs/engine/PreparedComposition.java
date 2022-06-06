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
        return new Of2<>(compositions.getOrCreateByType(new Class<?>[]{compType1, compType2}));
    }

    public static class Of {
        protected final DataComposition context;

        protected Object[] components;

        public Of(DataComposition context) {
            this.context = context;
        }

        public Object[] getComponents() {
            return components;
        }

        public Object getContext() {
            return context;
        }
    }

    public static class Of1<T> extends Of implements Composition.Of1<T> {

        public Of1(DataComposition context) {
            super(context);
        }

        @Override
        public Composition.Of withValue(T comp) {
            components = new Object[]{comp};
            return this;
        }
    }

    public static class Of2<T1, T2> extends Of implements Composition.Of2<T1, T2> {

        public Of2(DataComposition context) {
            super(context);
        }

        @Override
        public Composition.Of withValue(T1 comp1, T2 comp2) {
            return null;
        }
    }
}
