/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.test.engine.system;

import dev.dominion.ecs.engine.system.UncheckedUpdater;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UncheckedUpdaterTest {

    @Test
    void compareAndSetRef() throws NoSuchFieldException {

        AClass aClass = new AClass();
        UncheckedUpdater.Reference<AClass, Long> refUpdater =
                new UncheckedUpdater.Reference<>(AClass.class, "aField");
        Assertions.assertTrue(refUpdater.compareAndSet(aClass, aClass.aField, 10L));
        Assertions.assertEquals(10L, aClass.aField);
        Assertions.assertFalse(refUpdater.compareAndSet(aClass, 9L, 11L));
        Assertions.assertTrue(refUpdater.compareAndSet(aClass, 10L, 11L));
        Assertions.assertEquals(11L, aClass.aField);
    }

    @Test
    void compareAndSetInt() throws NoSuchFieldException {

        AClass aClass = new AClass();
        UncheckedUpdater.Int<AClass> intUpdater =
                new UncheckedUpdater.Int<>(AClass.class, "aIntField");
        Assertions.assertTrue(intUpdater.compareAndSet(aClass, aClass.aIntField, 10));
        Assertions.assertEquals(10, aClass.aIntField);
        Assertions.assertFalse(intUpdater.compareAndSet(aClass, 9, 11));
        Assertions.assertTrue(intUpdater.compareAndSet(aClass, 10, 11));
        Assertions.assertEquals(11, aClass.aIntField);
    }

    public static class AClass {
        private Long aField;
        private int aIntField;
    }
}