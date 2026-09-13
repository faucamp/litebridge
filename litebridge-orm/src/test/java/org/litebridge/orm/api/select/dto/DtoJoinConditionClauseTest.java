package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DtoJoinConditionClauseTest {

    private LitebridgeContext litebridgeContext;
    private JoinNode joinNode;
    private AtomicReference<QueryNode> capturedNode;

    @BeforeEach
    void setUp() {
        litebridgeContext = mock(LitebridgeContext.class);
        final SelectNode selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        joinNode = new JoinNode(selectNode, "INNER", SelectTestDto.class, null);
        capturedNode = new AtomicReference<>();
    }

    @Test
    void constructorAndCondition_withColumn() {
        // Given
        final DtoJoinConditionClause<SelectTestDto> clause = new DtoJoinConditionClause<>(
                litebridgeContext,
                LogicOperator.AND,
                "id",
                null,
                joinNode,
                node -> {
                    capturedNode.set(node);
                    return mock(DtoJoinConditionClauseTerminal.class);
                }
        );

        // When
        clause.eq(100L);

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals("id", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals(100L, conditionNode.rhs());
        assertSame(joinNode, conditionNode.previous());
    }

    @Test
    void constructorAndCondition_withExpression() {
        // Given
        final ExpressionSpec expr = Fn.column("age");
        final DtoJoinConditionClause<SelectTestDto> clause = new DtoJoinConditionClause<>(
                litebridgeContext,
                LogicOperator.OR,
                null,
                expr,
                joinNode,
                node -> {
                    capturedNode.set(node);
                    return mock(DtoJoinConditionClauseTerminal.class);
                }
        );

        // When
        clause.gt(18);

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }
}
