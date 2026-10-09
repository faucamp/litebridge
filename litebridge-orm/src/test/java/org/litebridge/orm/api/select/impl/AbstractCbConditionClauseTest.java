package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbDtoConditionClause;
import org.litebridge.orm.api.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AbstractCbConditionClauseTest {

    private CbDtoConditionClause<Object> clause;

    @BeforeEach
    void setUp() {
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final ExpressionSpec lhs = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        clause = new CbDtoConditionClause<>(
                litebridgeContext,
                LogicOperator.NOOP,
                null,
                lhs,
                null);
    }

    @Test
    void eq() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.eq("value");

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.EQ, node.operator());
        assertEquals("value", node.rhs());
    }

    @Test
    void neq() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.neq("value");

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.NEQ, node.operator());
    }

    @Test
    void lt() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.lt(10);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.LT, node.operator());
    }

    @Test
    void lte() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.lte(10);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.LTE, node.operator());
    }

    @Test
    void gt() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.gt(10);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.GT, node.operator());
    }

    @Test
    void gte() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.gte(10);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.GTE, node.operator());
    }

    @Test
    void like() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.like("%val%");

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.LIKE, node.operator());
    }

    @Test
    void notLike() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.notLike("%val%");

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.NOT_LIKE, node.operator());
    }

    @Test
    void in() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.in(1, 2, 3);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.IN, node.operator());
        assertEquals(List.of(1, 2, 3), node.rhs());
    }

    @Test
    void in_collection() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.in(Arrays.asList(4, 5));

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(List.of(4, 5), node.rhs());
    }

    @Test
    void notIn() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.notIn(1, 2);

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.NOT_IN, node.operator());
    }

    @Test
    void isNull() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.isNull();

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.IS_NULL, node.operator());
    }

    @Test
    void isNotNull() {
        // When
        final CbDtoConditionClauseTerminal<Object> terminal = clause.isNotNull();

        // Then
        final ConditionNode node = (ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal);
        assertEquals(Operator.IS_NOT_NULL, node.operator());
    }

    @Test
    void lte_invalidNullOperator() {
        assertThrows(IllegalArgumentException.class, () -> clause.lte((Object) null));
    }

    @Test
    void lt_subselectNullNotAllowed() {
        assertThrows(NullPointerException.class, () -> clause.lt((Function<SelectApi, SelectTerminal<?>>) null));
    }
}
