package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class DtoHavingConditionClauseTest {

    private LitebridgeContext litebridgeContext;
    private AtomicReference<QueryNode> capturedNode;

    @BeforeEach
    void setUp() {
        litebridgeContext = mock(LitebridgeContext.class);
        capturedNode = new AtomicReference<>();
    }

    @Test
    void constructorAndCondition_withField() {
        // Given
        final DtoHavingConditionClause<SelectTestDto> clause = new DtoHavingConditionClause<>(
                litebridgeContext,
                LogicOperator.AND,
                "age",
                null,
                null,
                node -> {
                    capturedNode.set(node);
                    return mock(DtoHavingConditionClauseTerminal.class);
                }
        );

        // When
        clause.eq(30);

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals(30, conditionNode.rhs());
    }

    @Test
    void constructorAndCondition_withExpression() {
        // Given
        final ExpressionSpec countExpr = Fn.count();
        final DtoHavingConditionClause<SelectTestDto> clause = new DtoHavingConditionClause<>(
                litebridgeContext,
                LogicOperator.OR,
                null,
                countExpr,
                null,
                node -> {
                    capturedNode.set(node);
                    return mock(DtoHavingConditionClauseTerminal.class);
                }
        );

        // When
        clause.isNull();

        // Then
        final QueryNode node = capturedNode.get();
        assertNotNull(node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, node);
        assertEquals(countExpr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.IS_NULL, conditionNode.operator());
        assertNull(conditionNode.rhs());
    }
}
