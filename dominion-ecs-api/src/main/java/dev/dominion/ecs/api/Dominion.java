/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * @author Enrico Stara
 */
public interface Dominion extends AutoCloseable {

    String DEFAULT_DOMINION_IMPLEMENTATION = "dev.dominion.ecs.engine.EntityRepository$Factory";

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
        return factory(DEFAULT_DOMINION_IMPLEMENTATION);
    }

    static Dominion.Factory factory(String implementation) {
        try {
            return ServiceLoader
                    .load(Dominion.Factory.class)
                    .stream()
                    .filter(p -> p.get().getClass().getName().contains(implementation))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Unable to load " + implementation))
                    .get();
        } catch (NoSuchElementException e) {
            try {
                return (Factory) Class.forName(implementation).getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
                ex.printStackTrace();
                throw e;
            }
        }
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

    /**
     * Retrieves the component of the specified type by finding all entities that have the component type
     *
     * @param type the component class
     * @param <T>  the component type
     * @return the query results
     */
    <T> Results<Results.Comp1<T>> findComponents(Class<T> type);

    /**
     * Retrieves all components of the given types by finding all entities that match the set of component types
     *
     * @param type1 the first component class
     * @param type2 the second component class
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @return the query results
     */
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
