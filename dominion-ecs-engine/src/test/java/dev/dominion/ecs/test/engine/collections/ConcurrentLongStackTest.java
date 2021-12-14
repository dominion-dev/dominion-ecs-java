package dev.dominion.ecs.test.engine.collections;

import dev.dominion.ecs.engine.collections.ConcurrentLongStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcurrentLongStackTest {

    @Test
    void pop() {
        ConcurrentLongStack stack = new ConcurrentLongStack(16);
        Assertions.assertEquals(Long.MIN_VALUE, stack.pop());
        stack.push(1);
        stack.push(2);
        Assertions.assertEquals(2, stack.pop());
        Assertions.assertEquals(1, stack.pop());
    }

    @Test
    void push() {
        ConcurrentLongStack stack = new ConcurrentLongStack(16);
        Assertions.assertTrue(stack.push(1));
        Assertions.assertTrue(stack.push(2));
        Assertions.assertFalse(stack.push(3));
    }
}
