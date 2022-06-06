/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

public interface Composition {

    <T> Of1<T> of(Class<T> compType);

    <T1, T2> Of2<T1, T2> of(Class<T1> compType1, Class<T2> compType2);

    sealed interface OfTypes permits Of1, Of2 {
        Object[] getComponents();

        Object getContext();
    }

    non-sealed interface Of1<T> extends OfTypes {
        OfTypes withValue(T comp);
    }

    non-sealed interface Of2<T1, T2> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2);
    }
}
