package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SqlWhereConditionClauseTest {

    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private AtomicReference<QueryNode> capturedNode;

    @BeforeEach
    void setUp() {
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, null, null, null, null, null);
        capturedNode = new AtomicReference<>();
    }

    @Test
    void constructorAndCondition_withColumn() {
        // Given
        final SqlWhereConditionClause clause = new SqlWhereConditionClause(
                litebridgeContext,
                LogicOperator.AND,
                "name",
                null,
                selectNode,
                node -> {
                    capturedNode.set(node);
                    return mock(SqlWhereConditionClauseTerminal.class);
                }
        );

        // When
        clause.eq("Alice");

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Alice", conditionNode.rhs());
        assertSame(selectNode, conditionNode.previous());
    }

    @Test
    void constructorAndCondition_withExpression() {
        // Given
        final ExpressionSpec expr = Fn.column("age");
        final SqlWhereConditionClause clause = new SqlWhereConditionClause(
                litebridgeContext,
                LogicOperator.OR,
                null,
                expr,
                null,
                node -> {
                    capturedNode.set(node);
                    return mock(SqlWhereConditionClauseTerminal.class);
                }
        );

        // When
        clause.isNull();

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.IS_NULL, conditionNode.operator());
        assertNull(conditionNode.rhs());
    }
}
