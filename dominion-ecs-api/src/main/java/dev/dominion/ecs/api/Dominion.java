/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

import dev.dominion.ecs.api.Results.*;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * A Dominion is an independent container for all ECS data. The User Application can create more than one Dominion with
 * different names. It is the entry point for using the library and provides methods for creating, finding, and deleting
 * items required by the user application.
 *
 * @author Enrico Stara
 */
public interface Dominion {

    String DEFAULT_DOMINION_IMPLEMENTATION = "dev.dominion.ecs.engine.EntityRepository$Factory";

    /**
     * Creates a new Dominion with an automatically assigned name.
     *
     * @return a new Dominion
     */
    static Dominion create() {
        return factory().create();
    }

    /**
     * Creates a new Dominion with the provided name.
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
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException ex) {
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

    /**
     * Creates a new Entity by passing a prepared composition of one or more POJO components.
     *
     * @param withValues a prepared composition with the required components
     * @return a new Entity
     */
    Entity createPreparedEntity(Composition.OfTypes withValues);

    /**
     * Creates a new Entity by using another Entity as prefab and adding zero or more POJO components.
     *
     * @param prefab     prefab Entity to start from
     * @param components zero o more POJO components assigned to the new Entity.
     * @return a new Entity
     */
    Entity createEntityAs(Entity prefab, Object... components);

    /**
     * Removes the entity by freeing the id and canceling the reference to all components, if any.
     *
     * @param entity the Entity to be deleted
     * @return false if the Entity has already been deleted
     */
    boolean deleteEntity(Entity entity);

    /**
     * Modifies the entity composition by adding components and/or removing component types
     *
     * @param modifier the modifier that has been prepared to modify the entity composition
     * @return true if the entity composition has been modified
     */
    boolean modifyEntity(Composition.Modifier modifier);

    /**
     * Provides the Composition class to support the creation of prepared entities
     *
     * @return the Composition instance
     */
    Composition composition();

    /**
     * Create a new Scheduler to submit/suspend/resume systems that are executed on every tick.
     *
     * @return a new scheduler
     */
    Scheduler createScheduler();

    /**
     * Finds all compositions with a component of the specified type.
     *
     * @param type the component class
     * @param <T>  the component type
     * @return the results
     */
    <T> Results<T> findCompositionsWith(Class<T> type);

    /**
     * Finds all compositions with components of the specified types.
     *
     * @param type1 the 1st component class
     * @param type2 the 2nd component class
     * @param <T1>  the 1st component type
     * @param <T2>  the 2nd component type
     * @return the results
     */
    <T1, T2> Results<With2<T1, T2>> findCompositionsWith(Class<T1> type1, Class<T2> type2);

    <T1, T2, T3> Results<With3<T1, T2, T3>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3);

    <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4);

    <T1, T2, T3, T4, T5> Results<With5<T1, T2, T3, T4, T5>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5);

    <T1, T2, T3, T4, T5, T6> Results<With6<T1, T2, T3, T4, T5, T6>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6);

    /**
     * Finds all entities with a component of the specified type.
     *
     * @param type the component class
     * @param <T>  the component type
     * @return the results
     */
    <T> Results<With1<T>> findEntitiesWith(Class<T> type);

    /**
     * Finds all entities with components of the specified types.
     *
     * @param type1 the 1st component class
     * @param type2 the 2nd component class
     * @param <T1>  the 1st component type
     * @param <T2>  the 2nd component type
     * @return the results
     */
    <T1, T2> Results<With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2);

    <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3);

    <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4);

    <T1, T2, T3, T4, T5> Results<With5<T1, T2, T3, T4, T5>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5);

    <T1, T2, T3, T4, T5, T6> Results<With6<T1, T2, T3, T4, T5, T6>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6);

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    void close();

    interface Factory {
        Dominion create();

        Dominion create(String name);
    }
}
