package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ConditionNodeUtilTest {

    @Test
    void privateConstructorThrowsOrIsPrivate() throws Exception {
        // Given
        final Constructor<ConditionNodeUtil> constructor = ConditionNodeUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        // When
        constructor.setAccessible(true);
        final ConditionNodeUtil instance = constructor.newInstance();

        // Then
        assertEquals(ConditionNodeUtil.class, instance.getClass());
    }

    @Test
    void valueStructuralKeyForNullAndDefaultScalars() {
        // Given / When / Then
        assertEquals(1, ConditionNodeUtil.valueStructuralKey(null));
        assertEquals(1, ConditionNodeUtil.valueStructuralKey("string"));
        assertEquals(1, ConditionNodeUtil.valueStructuralKey(123));
        assertEquals(1, ConditionNodeUtil.valueStructuralKey(true));
    }

    @Test
    void valueStructuralKeyForCollectionsReturnsSize() {
        // Given
        final List<String> list = List.of("a", "b", "c");
        final Set<Integer> emptySet = Set.of();

        // When / Then
        assertEquals(3, ConditionNodeUtil.valueStructuralKey(list));
        assertEquals(0, ConditionNodeUtil.valueStructuralKey(emptySet));
    }

    @Test
    void valueStructuralKeyForQueryNodeReturnsNodeItself() {
        // Given
        final QueryNode queryNode = new DeleteNode(null, "q1", null);

        // When / Then
        assertSame(queryNode, ConditionNodeUtil.valueStructuralKey(queryNode));
    }

    @Test
    void valueStructuralKeyForSelectTerminalExtractsNode() {
        // Given
        final SelectTerminal<?> terminal = mock(SelectTerminal.class);
        final QueryNode internalNode = new DeleteNode(null, "q2", null);

        try (final var mockedStatic = mockStatic(SelectTerminalInspector.class)) {
            mockedStatic.when(() -> SelectTerminalInspector.getNode(terminal)).thenReturn(internalNode);

            // When
            final Object result = ConditionNodeUtil.valueStructuralKey(terminal);

            // Then
            assertSame(internalNode, result);
        }
    }
}
