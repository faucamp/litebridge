package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.tracking.FieldAccessor;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoJoinClauseTest {

    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private AtomicReference<QueryNode> capturedConditionNode;
    private DtoJoinClause<SelectTestDto> joinClause;

    @BeforeEach
    void setUp() {
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        capturedConditionNode = new AtomicReference<>();
        joinClause = new DtoJoinClause<>(selectNode, litebridgeContext, conditionNode -> {
            capturedConditionNode.set(conditionNode);
            return mock(DtoJoinConditionClauseTerminal.class);
        });
    }

    @Test
    void on_withString() {
        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> terminal = joinClause.on("userId");

        // Then
        assertNotNull(terminal);
        final QueryNode node = capturedConditionNode.get();
        final ConditionJoinUsingNode joinUsingNode = assertInstanceOf(ConditionJoinUsingNode.class, node);
        assertNull(joinUsingNode.previous());
        assertEquals(LogicOperator.NOOP, joinUsingNode.logicOperator());
        assertEquals("userId", joinUsingNode.usingColumn());
    }

    @Test
    void on_withQueryField() {
        // Given
        final QueryField queryField = new QueryField(SelectTestDto.class, "name");

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> terminal = joinClause.on(queryField);

        // Then
        assertNotNull(terminal);
        final ConditionJoinUsingNode joinUsingNode = assertInstanceOf(ConditionJoinUsingNode.class, capturedConditionNode.get());
        assertEquals("name", joinUsingNode.usingColumn());
    }

    @Test
    void on_withProtoExpressionSpec() {
        // Given
        final ProtoExpressionSpec protoExpr = (ProtoExpressionSpec) Fn.column("age");

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> terminal = joinClause.on(protoExpr);

        // Then
        assertNotNull(terminal);
        final ConditionJoinUsingNode joinUsingNode = assertInstanceOf(ConditionJoinUsingNode.class, capturedConditionNode.get());
        assertEquals("age", joinUsingNode.usingColumn());
    }

    @Test
    void on_withSelectFieldSpec() {
        // Given
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(fieldAccessor.name()).thenReturn("id");
        final Column column = mock(Column.class);
        final SelectFieldSpec selectFieldSpec = new SelectFieldSpec(fieldAccessor, column);

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> terminal = joinClause.on(selectFieldSpec);

        // Then
        assertNotNull(terminal);
        final ConditionJoinUsingNode joinUsingNode = assertInstanceOf(ConditionJoinUsingNode.class, capturedConditionNode.get());
        assertEquals("id", joinUsingNode.usingColumn());
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
    void on_withQueryConditionBuilder() {
        // Given
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").eq(20);

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> terminal = joinClause.on(builder);

        // Then
        assertNotNull(terminal);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, capturedConditionNode.get());
        assertNull(groupNode.previous());
        assertEquals(LogicOperator.NOOP, groupNode.logicOperator());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals(20, conditionNode.rhs());
    }
}
