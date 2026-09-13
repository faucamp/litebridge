package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SqlJoinClauseTest {

    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private AtomicReference<QueryNode> capturedConditionNode;
    private SqlJoinClause joinClause;

    @BeforeEach
    void setUp() {
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, null, null, null, null, null);
        capturedConditionNode = new AtomicReference<>();
        joinClause = new SqlJoinClause("orders", selectNode, litebridgeContext, conditionNode -> {
            capturedConditionNode.set(conditionNode);
            return mock(SqlJoinConditionClauseTerminal.class);
        });
    }

    @Test
    void on_withString() {
        // When
        final SqlJoinConditionClause conditionClause = joinClause.on("user_id");

        // Then
        assertNotNull(conditionClause);
        conditionClause.eq(10L);
        final QueryNode node = capturedConditionNode.get();
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertSame(selectNode, conditionNode.previous());
        assertEquals("user_id", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals(10L, conditionNode.rhs());
    }

    @Test
    void on_withProtoExpressionSpec() {
        // Given
        final ProtoExpressionSpec protoExpr = (ProtoExpressionSpec) Fn.column("age");

        // When
        final SqlJoinConditionClause conditionClause = joinClause.on(protoExpr);

        // Then
        assertNotNull(conditionClause);
        conditionClause.gt(18);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, capturedConditionNode.get());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void on_withSelectColumnSpec() {
        // Given
        final Column column = new Column(new Table("users"), "id");
        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(column);

        // When
        final SqlJoinConditionClause conditionClause = joinClause.on(selectColumnSpec);

        // Then
        assertNotNull(conditionClause);
        conditionClause.eq(5L);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, capturedConditionNode.get());
        assertEquals("id", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals(5L, conditionNode.rhs());
    }

    @Test
    void on_withUnsupportedExpression_throwsException() {
        // Given
        final ExpressionSpec unsupportedExpr = Fn.count();

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> joinClause.on(unsupportedExpr));
        assertEquals("Unsupported JOIN ON expression: " + unsupportedExpr, ex.getMessage());
    }

    @Test
    void using() {
        // When
        final SqlJoinConditionClauseTerminal terminal = joinClause.using("user_id");

        // Then
        assertNotNull(terminal);
        final QueryNode node = capturedConditionNode.get();
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertSame(selectNode, conditionNode.previous());
        assertEquals(LogicOperator.NOOP, conditionNode.logicOperator());
        assertEquals("user_id", conditionNode.lhsColumn());
        assertEquals(Operator.USING, conditionNode.operator());
        assertEquals("user_id", conditionNode.rhs());
    }
}
