package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextStackTest {

    private static class TestContextStack extends ContextStack<String> {
        private int counter = 0;

        @Override
        protected String newRootInstance() {
            return "root-" + (++counter);
        }

        @Override
        protected String newSubInstance(final LogicOperator logicOperator) {
            return logicOperator.name() + "-" + (++counter);
        }
    }

    @Test
    void initialStackIsEmpty() {
        // Given
        final TestContextStack stack = new TestContextStack();

        // When / Then
        assertTrue(stack.isEmpty());
        assertTrue(stack.all().isEmpty());
    }

    @Test
    void currentCreatesAndReturnsRootInstanceWhenEmpty() {
        // Given
        final TestContextStack stack = new TestContextStack();

        // When
        final String root = stack.current();

        // Then
        assertEquals("root-1", root);
        assertFalse(stack.isEmpty());
        assertEquals(List.of("root-1"), stack.all());

        // When subsequent call
        final String rootAgain = stack.current();

        // Then
        assertSame(root, rootAgain);
        assertEquals(1, stack.all().size());
    }

    @Test
    void pushAddsSubInstanceAndPushesToStack() {
        // Given
        final TestContextStack stack = new TestContextStack();
        final String root = stack.current();

        // When
        final String sub1 = stack.push(LogicOperator.AND);

        // Then
        assertEquals("AND-2", sub1);
        assertSame(sub1, stack.current());
        assertEquals(List.of(root, sub1), stack.all());

        // When pushing another
        final String sub2 = stack.push(LogicOperator.OR);

        // Then
        assertEquals("OR-3", sub2);
        assertSame(sub2, stack.current());
        assertEquals(List.of(root, sub1, sub2), stack.all());
    }

    @Test
    void popRestoresPreviousContextOnStack() {
        // Given
        final TestContextStack stack = new TestContextStack();
        final String root = stack.current();
        final String sub = stack.push(LogicOperator.AND);

        // When
        stack.pop();

        // Then
        assertSame(root, stack.current());
        assertEquals(List.of(root, sub), stack.all());
        assertFalse(stack.isEmpty());
    }
}
