/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * An independent container for all ECS data. The User Application can create more than one _Dominion_ with different
 * names. It is the entry point for using the library and provides methods for creating, finding, and deleting items
 * required by the user application.
 *
 * @author Enrico Stara
 */
public interface Dominion extends AutoCloseable {

    /**
     * Creates a new Dominion with an automatically assigned name
     *
     * @return a new Dominion
     */
    static Dominion create() {
        return factory().create();
    }

    /**
     * Creates a new Dominion with the provided name
     *
     * @param name the name of the new Dominion
     * @return a new Dominion
     */
    static Dominion create(String name) {
        return factory().create(name);
    }

    static Dominion.Factory factory() {
        return factory("dev.dominion.ecs.engine");
    }

    static Dominion.Factory factory(String implementation) {
        return ServiceLoader
                .load(Dominion.Factory.class)
                .stream()
                .filter(p -> p.get().getClass().getName().contains(implementation))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Unable to load " + implementation))
                .get();
    }

    /**
     * Returns the Dominion name
     *
     * @return the name
     */
    String getName();

    /**
     * Creates a new Entity by adding zero or more POJO components.
     *
     * @param components zero o more POJO components assigned to the new Entity.
     * @return a new Entity
     */
    Entity createEntity(Object... components);

    Entity createEntityAs(Entity prefab, Object... components);

    /**
     * Delete the entity by freeing the id and canceling the reference to all components, if any
     *
     * @param entity the Entity to be deleted
     * @return false if the Entity has already been deleted
     */
    boolean deleteEntity(Entity entity);

    <T> Results<Results.Comp1<T>> findComponents(Class<T> type);

    <T1, T2> Results<Results.Comp2<T1, T2>> findComponents(Class<T1> type1, Class<T2> type2);

    <T1, T2, T3> Results<Results.Comp3<T1, T2, T3>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3);

    <T1, T2, T3, T4> Results<Results.Comp4<T1, T2, T3, T4>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4);

    <T1, T2, T3, T4, T5> Results<Results.Comp5<T1, T2, T3, T4, T5>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5);

    <T1, T2, T3, T4, T5, T6> Results<Results.Comp6<T1, T2, T3, T4, T5, T6>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6);

    interface Factory {
        Dominion create();

        Dominion create(String name);
    }
}
