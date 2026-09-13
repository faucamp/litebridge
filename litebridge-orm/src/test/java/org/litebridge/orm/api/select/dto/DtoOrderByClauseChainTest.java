package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DtoOrderByClauseChainTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private OrderByNode initialOrderNode;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        final SelectNode selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        initialOrderNode = new OrderByNode(selectNode, "name", null, true);
    }

    @Test
    void then_withFields() {
        // Given
        final DtoOrderByClauseChain<SelectTestDto> chain = new DtoOrderByClauseChain<>(initialOrderNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClause<SelectTestDto> nextClause = chain.then("age");

        // Then
        assertNotNull(nextClause);
        final DtoOrderByClauseChain<SelectTestDto> nextChain = nextClause.asc();
        final QueryNode node = SelectTerminalInspector.getNode(nextChain);
        final OrderByNode secondOrder = assertInstanceOf(OrderByNode.class, node);
        assertEquals("age", secondOrder.column());
        assertTrue(secondOrder.ascending());
        assertSame(initialOrderNode, secondOrder.previous());
    }

    @Test
    void then_withExpressions() {
        // Given
        final DtoOrderByClauseChain<SelectTestDto> chain = new DtoOrderByClauseChain<>(initialOrderNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoOrderByClause<SelectTestDto> nextClause = chain.then(expr);

        // Then
        assertNotNull(nextClause);
        final DtoOrderByClauseChain<SelectTestDto> nextChain = nextClause.asc();
        final QueryNode node = SelectTerminalInspector.getNode(nextChain);
        final OrderByNode secondOrder = assertInstanceOf(OrderByNode.class, node);
        assertEquals(expr, secondOrder.expression());
        assertTrue(secondOrder.ascending());
        assertSame(initialOrderNode, secondOrder.previous());
    }
}
