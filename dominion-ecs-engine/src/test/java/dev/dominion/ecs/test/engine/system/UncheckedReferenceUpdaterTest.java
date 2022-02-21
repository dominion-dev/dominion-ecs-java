/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.test.engine.system;

import dev.dominion.ecs.engine.system.UncheckedReferenceUpdater;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UncheckedReferenceUpdaterTest {

    @Test
    void compareAndSet() throws NoSuchFieldException {

        AClass aClass = new AClass();
        UncheckedReferenceUpdater<AClass, Long> updater =
                new UncheckedReferenceUpdater<>(AClass.class, "aField");
        Assertions.assertTrue(updater.compareAndSet(aClass, aClass.aField, 10L));
        Assertions.assertEquals(10L, aClass.aField);
        Assertions.assertFalse(updater.compareAndSet(aClass, 9L, 11L));
        Assertions.assertTrue(updater.compareAndSet(aClass, 10L, 11L));
        Assertions.assertEquals(11L, aClass.aField);
    }

    public static class AClass {
        private Long aField;
    }
}