package org.litebridgedb.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.LiteralExpressionFactory;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.LogicCondition;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.sql.SqlJoinSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;
import org.litebridgedb.orm.expression.TestSelectReferenceExpressionFactory;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoinSpecTest {

    @Test
    void table() {
        // Given
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE", null);
        final JoinSpec joinSpec = new SqlJoinSpec(table, mock(SelectExpressionMapper.class));

        // When
        final Table result = joinSpec.table();

        // Then
        assertEquals(table, result);
    }

    @Test
    void newCondition() {
        // Given
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), mock(SelectExpressionMapper.class));
        final Table table = joinSpec.table();
        final ExpressionSpec expressionSpec = new SelectColumnSpec(new Column(table, "TEST_COLUMN"));

        // When
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, expressionSpec);

        // Then
        assertNotNull(conditionSpec);
    }

    @Test
    void using() {
        // Given
        final SelectExpressionMapper selectExpressionMapper = mock(SelectExpressionMapper.class);
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), selectExpressionMapper);

        when(selectExpressionMapper.resolveProtoExpression(any(ExpressionSpec.class), eq(ClauseType.WHERE)))
                .thenReturn(List.of(new SelectColumnSpec(new Column(new Table("TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN"))));
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select select = mock(SqlFunctionRegistry.Select.class);
        final LiteralExpressionFactory literalExpressionFactory = mock(LiteralExpressionFactory.class);
        when(selectExpressionMapper.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(sqlFunctionRegistry.select()).thenReturn(select);
        when(select.literal()).thenReturn(literalExpressionFactory);

        // When
        final ConditionSpec conditionSpec = joinSpec.using("TEST_COLUMN");
        final LogicCondition result = joinSpec.toJoin().conditions().conditions().getFirst();

        // Then
        assertNotNull(result);
        assertEquals(LogicOperator.NOOP, result.logicOperator());
        assertEquals(Operator.USING, result.condition().operator());
    }

    @Test
    void toJoin() {
        // Given
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);
        final ProtoExpressionResolver protoExpressionResolver = mock(ProtoExpressionResolver.class);
        when(protoExpressionResolver.resolveExpression(any(ExpressionSpec.class), any(ClauseType.class))).thenAnswer(i -> Stream.of((ExpressionSpec) i.getArgument(0)));
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver));
        final Table table = joinSpec.table();
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(new Column(table, "TEST_COLUMN")));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);

        // When
        final Join result = joinSpec.toJoin();

        // Then
        assertNotNull(result);
        assertSame(table, result.table());
        assertNotNull(result.conditions());
        assertFalse(result.conditions().isEmpty());
    }
}