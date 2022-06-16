/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

public interface Composition {

    <T> Of1<T> of(Class<T> compType);

    <T1, T2> Of2<T1, T2> of(Class<T1> compType1, Class<T2> compType2);

    <T1, T2, T3> Of3<T1, T2, T3> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3);

    <T1, T2, T3, T4> Of4<T1, T2, T3, T4> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4);

    <T1, T2, T3, T4, T5> Of5<T1, T2, T3, T4, T5> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5);

    <T1, T2, T3, T4, T5, T6> Of6<T1, T2, T3, T4, T5, T6> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6);

    <T1, T2, T3, T4, T5, T6, T7> Of7<T1, T2, T3, T4, T5, T6, T7> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7);

    <T1, T2, T3, T4, T5, T6, T7, T8> Of8<T1, T2, T3, T4, T5, T6, T7, T8> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7, Class<T8> compType8);

    sealed interface OfTypes permits Of1, Of2, Of3, Of4, Of5, Of6, Of7, Of8 {
        Object[] getComponents();

        Object getContext();
    }

    non-sealed interface Of1<T> extends OfTypes {
        OfTypes withValue(T comp);
    }

    non-sealed interface Of2<T1, T2> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2);
    }

    non-sealed interface Of3<T1, T2, T3> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3);
    }

    non-sealed interface Of4<T1, T2, T3, T4> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4);
    }

    non-sealed interface Of5<T1, T2, T3, T4, T5> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5);
    }

    non-sealed interface Of6<T1, T2, T3, T4, T5, T6> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6);
    }

    non-sealed interface Of7<T1, T2, T3, T4, T5, T6, T7> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7);
    }

    non-sealed interface Of8<T1, T2, T3, T4, T5, T6, T7, T8> extends OfTypes {
        OfTypes withValue(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7, T8 comp8);
    }
}
