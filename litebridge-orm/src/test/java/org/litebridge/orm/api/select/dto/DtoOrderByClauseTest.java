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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class DtoOrderByClauseTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
    }

    @Test
    void asc_withColumns() {
        // Given
        final String[] columns = new String[]{"name", "age"};
        final DtoOrderByClause<SelectTestDto> clause = new DtoOrderByClause<>(columns, selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClauseChain<SelectTestDto> chain = clause.asc();

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        final OrderByNode lastOrder = assertInstanceOf(OrderByNode.class, node);
        assertEquals("age", lastOrder.column());
        assertNull(lastOrder.expression());
        assertTrue(lastOrder.ascending());

        final OrderByNode firstOrder = assertInstanceOf(OrderByNode.class, lastOrder.previous());
        assertEquals("name", firstOrder.column());
        assertNull(firstOrder.expression());
        assertTrue(firstOrder.ascending());
        assertSame(selectNode, firstOrder.previous());
    }

    @Test
    void desc_withColumns() {
        // Given
        final String[] columns = new String[]{"id"};
        final DtoOrderByClause<SelectTestDto> clause = new DtoOrderByClause<>(columns, selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClauseChain<SelectTestDto> chain = clause.desc();

        // Then
        final OrderByNode orderNode = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertEquals("id", orderNode.column());
        assertNull(orderNode.expression());
        assertFalse(orderNode.ascending());
        assertSame(selectNode, orderNode.previous());
    }

    @Test
    void asc_withExpressions() {
        // Given
        final ExpressionSpec expr1 = Fn.column("name");
        final ExpressionSpec expr2 = Fn.column("age");
        final DtoOrderByClause<SelectTestDto> clause = new DtoOrderByClause<>(new ExpressionSpec[]{expr1, expr2}, selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClauseChain<SelectTestDto> chain = clause.asc();

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        final OrderByNode lastOrder = assertInstanceOf(OrderByNode.class, node);
        assertNull(lastOrder.column());
        assertEquals(expr2, lastOrder.expression());
        assertTrue(lastOrder.ascending());

        final OrderByNode firstOrder = assertInstanceOf(OrderByNode.class, lastOrder.previous());
        assertNull(firstOrder.column());
        assertEquals(expr1, firstOrder.expression());
        assertTrue(firstOrder.ascending());
        assertSame(selectNode, firstOrder.previous());
    }

    @Test
    void desc_withExpressions() {
        // Given
        final ExpressionSpec expr = Fn.column("age");
        final DtoOrderByClause<SelectTestDto> clause = new DtoOrderByClause<>(new ExpressionSpec[]{expr}, selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClauseChain<SelectTestDto> chain = clause.desc();

        // Then
        final OrderByNode orderNode = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertNull(orderNode.column());
        assertEquals(expr, orderNode.expression());
        assertFalse(orderNode.ascending());
        assertSame(selectNode, orderNode.previous());
    }
}
