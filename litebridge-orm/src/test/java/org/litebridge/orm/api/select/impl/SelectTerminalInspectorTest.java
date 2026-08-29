//package org.litebridge.orm.api.select.impl;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.orm.api.select.SelectTerminal;
//import org.litebridge.orm.engine.ast.QueryNode;
//import org.litebridge.orm.engine.ast.SelectNode;
//import org.litebridge.orm.expression.ExpressionSpec;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class SelectTerminalInspectorTest {
//
//    @Test
//    void getNode() {
//        // Given
//        final AbstractSelector<?, ?> abstractSelector = mock(AbstractSelector.class);
//        final QueryNode node = new SelectNode(null, new ExpressionSpec[0], null);
//        when(abstractSelector.node()).thenReturn(node);
//        final DelegatingSelectTerminal<?, ?> delegatingSelector = new DelegatingSelectTerminal<>(abstractSelector);
//
//        // When
//        final QueryNode result = SelectTerminalInspector.getNode(delegatingSelector);
//
//        // Then
//        assertEquals(node, result);
//    }
//
//    @Test
//    void testUnsupportedTerminal() {
//        final SelectTerminal<?> unsupported = mock(SelectTerminal.class);
//        assertThrows(IllegalStateException.class, () -> SelectTerminalInspector.getNode(unsupported));
//    }
//}
