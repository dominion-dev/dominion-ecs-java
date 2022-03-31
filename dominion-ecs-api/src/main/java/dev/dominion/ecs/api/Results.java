/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Results is the output of the Dominion::findComponents method list and represents a simple container of all entities
 * that match a set of components and, optionally, have a specified state. Results can be further filtered by specifying
 * one or more component types to exclude. Both iterator and stream methods are available to retrieve found entities in
 * sequence.
 *
 * @param <T> the type of each fetched row
 * @author Enrico Stara
 */
public interface Results<T> {

    Iterator<T> iterator();

    Stream<T> stream();

    Results<T> excludeWith(Class<?>... componentTypes);

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
