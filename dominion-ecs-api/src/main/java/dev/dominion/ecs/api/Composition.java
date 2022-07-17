/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.api;

/**
 * A Composition is the aggregation of components of which an entity can be made of.
 * Provides methods to prepare for entity creation with a certain component type composition or to prepare for entity
 * change by specifying which component type is to be added and / or removed.
 *
 * <pre>
 *     Dominion dominion = Dominion.create();
 *     Composition composition = dominion.composition();
 *
 *     // prepared entity creations
 *     var compositionOf1 = composition.of(Comp.class);
 *     Entity entity1 = dominion.createPreparedEntity(compositionOf1.withValue(new Comp(0)));
 *     Entity entity2 = dominion.createPreparedEntity(compositionOf1.withValue(new Comp(1)));
 *
 *     // prepared entity changes
 *     var modifyAdding1 = composition.byAdding1AndRemoving(Comp2.class);
 *     dominion.modifyEntity(modifyAdding1.withValue(entity1, new Comp2()));
 *     dominion.modifyEntity(modifyAdding1.withValue(entity2, new Comp2()));
 * </pre>
 *
 * @author Enrico Stara
 */
public interface Composition {

    /**
     * Provides a prepared composition of a single component type
     *
     * @param compType the component class
     * @param <T>      the component type
     * @return the prepared composition
     */
    <T> Of1<T> of(Class<T> compType);

    /**
     * Provides a prepared composition of two component types
     *
     * @param compType1 the 1st component class
     * @param compType2 the 2nd component class
     * @param <T1>      the 1st component type
     * @param <T2>      the 2nd component type
     * @return the prepared composition
     */
    <T1, T2> Of2<T1, T2> of(Class<T1> compType1, Class<T2> compType2);

    <T1, T2, T3> Of3<T1, T2, T3> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3);

    <T1, T2, T3, T4> Of4<T1, T2, T3, T4> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4);

    <T1, T2, T3, T4, T5> Of5<T1, T2, T3, T4, T5> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5);

    <T1, T2, T3, T4, T5, T6> Of6<T1, T2, T3, T4, T5, T6> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6);

    <T1, T2, T3, T4, T5, T6, T7> Of7<T1, T2, T3, T4, T5, T6, T7> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7);

    <T1, T2, T3, T4, T5, T6, T7, T8> Of8<T1, T2, T3, T4, T5, T6, T7, T8> of(Class<T1> compType1, Class<T2> compType2, Class<T3> compType3, Class<T4> compType4, Class<T5> compType5, Class<T6> compType6, Class<T7> compType7, Class<T8> compType8);

    /**
     * Provides a prepared modifier to remove one or more component types
     *
     * @param removedCompTypes the component class list to be removed
     * @return the prepared modifier
     */
    ByRemoving byRemoving(Class<?>... removedCompTypes);

    /**
     * Provides a prepared modifier to add one component type and optionally remove one or more component types
     *
     * @param addedCompType    the component class to be added
     * @param removedCompTypes the component type list to be removed (might be empty)
     * @param <T>              the component type to be added
     * @return the prepared modifier
     */
    <T> ByAdding1AndRemoving<T> byAdding1AndRemoving(Class<T> addedCompType, Class<?>... removedCompTypes);

    /**
     * Provides a prepared modifier to add two component types and optionally remove one or more component types
     *
     * @param addedCompType1   the 1st component class to be added
     * @param addedCompType2   the 2nd component class to be added
     * @param removedCompTypes the component type list to be removed (might be empty)
     * @param <T1>             the 1st component type to be added
     * @param <T2>             the 2nd component type to be added
     * @return the prepared modifier
     */
    <T1, T2> ByAdding2AndRemoving<T1, T2> byAdding2AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<?>... removedCompTypes);

    <T1, T2, T3> ByAdding3AndRemoving<T1, T2, T3> byAdding3AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<?>... removedCompTypes);

    <T1, T2, T3, T4> ByAdding4AndRemoving<T1, T2, T3, T4> byAdding4AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5> ByAdding5AndRemoving<T1, T2, T3, T4, T5> byAdding5AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6> ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> byAdding6AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6, T7> ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> byAdding7AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<T7> addedCompType7, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6, T7, T8> ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> byAdding8AndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<T7> addedCompType7, Class<T8> addedCompType8, Class<?>... removedCompTypes);

    /**
     * The prepared composition abstraction
     */
    sealed interface OfTypes permits Of1, Of2, Of3, Of4, Of5, Of6, Of7, Of8 {
        Object[] getComponents();

        Object getContext();
    }

    /**
     * The prepared composition of a single component type
     *
     * @param <T> the component type
     */
    non-sealed interface Of1<T> extends OfTypes {
        OfTypes withValue(T comp);
    }

    /**
     * The prepared composition of two component types
     *
     * @param <T1> the 1st component type
     * @param <T2> the 2nd component type
     */
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

    /**
     * The prepared modifier abstraction
     */
    sealed interface Modifier permits ByRemoving, ByAdding1AndRemoving, ByAdding2AndRemoving, ByAdding3AndRemoving, ByAdding4AndRemoving, ByAdding5AndRemoving, ByAdding6AndRemoving, ByAdding7AndRemoving, ByAdding8AndRemoving {
        Object getModifier();
    }

    /**
     * The prepared modifier to remove one or more component types
     */
    non-sealed interface ByRemoving extends Modifier {
        Modifier withValue(Entity entity);
    }

    /**
     * The prepared modifier to add one component type and optionally remove one or more component types
     *
     * @param <T> the component type to be added
     */
    non-sealed interface ByAdding1AndRemoving<T> extends Modifier {
        Modifier withValue(Entity entity, T comp);
    }

    /**
     * The prepared modifier to add two component types and optionally remove one or more component types
     *
     * @param <T1> the 1st component type to be added
     * @param <T2> the 2nd component type to be added
     */
    non-sealed interface ByAdding2AndRemoving<T1, T2> extends Modifier {
        Modifier withValue(Entity entity, T1 comp1, T2 comp2);
    }

    non-sealed interface ByAdding3AndRemoving<T1, T2, T3> extends Modifier {
        Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3);
    }

    non-sealed interface ByAdding4AndRemoving<T1, T2, T3, T4> extends Modifier {
        Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4);
    }

    non-sealed interface ByAdding5AndRemoving<T1, T2, T3, T4, T5> extends Modifier {
        Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5);
    }

    non-sealed interface ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> extends Modifier {
        Modifier withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6);
    }

    non-sealed interface ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> extends Modifier {
        ByRemoving withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7);
    }

    non-sealed interface ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> extends Modifier {
        ByRemoving withValue(Entity entity, T1 comp1, T2 comp2, T3 comp3, T4 comp4, T5 comp5, T6 comp6, T7 comp7, T8 comp8);
    }
}
