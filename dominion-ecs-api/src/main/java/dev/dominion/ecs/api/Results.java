/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A Results instance is the output of the Dominion::findComponents method list and represents a simple container of all entities
 * that match a set of components and, optionally, have a specified state. Results can be further filtered by specifying
 * one or more component types to exclude. Both iterator and stream methods are available to retrieve found entities in
 * sequence.
 *
 * @param <T> the type of each result
 * @author Enrico Stara
 */
public interface Results<T> {

    /**
     * Provides an iterator to retrieve found entities in sequence.
     *
     * @return the iterator
     */
    Iterator<T> iterator();

    /**
     * Creates a sequential stream to supports functional-style operations on found entities.
     *
     * @return the stream
     */
    Stream<T> stream();

    /**
     * Provides a filtered Results without one or more component types to exclude.
     *
     * @param componentTypes one or more component types to exclude
     * @return the Results without excluded component types
     */
    Results<T> without(Class<?>... componentTypes);

    /**
     * Provides a Results also considering one or more types of components as a filter.
     *
     * @param componentTypes one or more types of components used as a filter
     * @return the Results also considering one or more types of components as a filter.
     */
    Results<T> withAlso(Class<?>... componentTypes);

    /**
     * Provides a filtered Results with only entities having the required state.
     *
     * @param state the requested state
     * @param <S>   the state enumeration type
     * @return the Results with only entities having the required state
     */
    <S extends Enum<S>> Results<T> withState(S state);

    record With1<T>(T comp, Entity entity) {
    }

    record With2<T1, T2>(T1 comp1, T2 comp2, Entity entity) {
    }

    record With3<T1, T2, T3>(T1 comp1, T2 comp2, T3 comp3, Entity entity) {
    }

    record With4<T1, T2, T3, T4>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, Entity entity) {
    }

    record With5<T1, T2, T3, T4, T5>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, Entity entity) {
    }

    record With6<T1, T2, T3, T4, T5, T6>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, Entity entity) {
    }
}
