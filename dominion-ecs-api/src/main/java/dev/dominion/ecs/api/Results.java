/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import java.util.Iterator;
import java.util.stream.Stream;

public interface Results<T> {

    Iterator<T> iterator();

    Stream<T> stream();

    Results<T> filter(Class<?>... componentTypes);

    record Comp1<T>(T comp, Entity entity) {
    }

    record Comp2<T1, T2>(T1 comp1, T2 comp2, Entity entity) {
    }

    record Comp3<T1, T2, T3>(T1 comp1, T2 comp2, T3 comp3, Entity entity) {
    }

    record Comp4<T1, T2, T3, T4>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, Entity entity) {
    }

    record Comp5<T1, T2, T3, T4, T5>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, Entity entity) {
    }

    record Comp6<T1, T2, T3, T4, T5, T6>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, Entity entity) {
    }
}
