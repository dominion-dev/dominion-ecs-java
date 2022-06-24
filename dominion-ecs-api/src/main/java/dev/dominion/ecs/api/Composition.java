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


    ByRemoving byRemoving(Class<?>... removedCompTypes);

    <T> ByAdding1AndRemoving<T> byAddingAndRemoving(Class<T> addedCompType, Class<?>... removedCompTypes);

    <T1, T2> ByAdding2AndRemoving<T1, T2> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<?>... removedCompTypes);

    <T1, T2, T3> ByAdding3AndRemoving<T1, T2, T3> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<?>... removedCompTypes);

    <T1, T2, T3, T4> ByAdding4AndRemoving<T1, T2, T3, T4> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5> ByAdding5AndRemoving<T1, T2, T3, T4, T5> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6> ByAdding6AndRemoving<T1, T2, T3, T4, T5, T6> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6, T7> ByAdding7AndRemoving<T1, T2, T3, T4, T5, T6, T7> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<T7> addedCompType7, Class<?>... removedCompTypes);

    <T1, T2, T3, T4, T5, T6, T7, T8> ByAdding8AndRemoving<T1, T2, T3, T4, T5, T6, T7, T8> byAddingAndRemoving(Class<T1> addedCompType1, Class<T2> addedCompType2, Class<T3> addedCompType3, Class<T4> addedCompType4, Class<T5> addedCompType5, Class<T6> addedCompType6, Class<T7> addedCompType7, Class<T8> addedCompType8, Class<?>... removedCompTypes);


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


    sealed interface Modifier permits ByRemoving, ByAdding1AndRemoving, ByAdding2AndRemoving, ByAdding3AndRemoving, ByAdding4AndRemoving, ByAdding5AndRemoving, ByAdding6AndRemoving, ByAdding7AndRemoving, ByAdding8AndRemoving {
        Object getModifier();
    }

    non-sealed interface ByRemoving extends Modifier {
        Modifier withValue(Entity entity);
    }

    non-sealed interface ByAdding1AndRemoving<T> extends Modifier {
        Modifier withValue(Entity entity, T comp);
    }

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
