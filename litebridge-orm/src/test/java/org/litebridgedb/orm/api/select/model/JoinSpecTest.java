package org.litebridgedb.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.sql.SqlJoinSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;
import org.litebridgedb.orm.expression.TestSelectReferenceExpressionFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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

        // When
        final ConditionSpec conditionSpec = joinSpec.newCondition(new Column(table, "TEST_COLUMN"));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);
        final List<ConditionSpec> result = joinSpec.conditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
    }

    @Test
    void using() {
        // Given
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), mock(SelectExpressionMapper.class));
        final Table table = joinSpec.table();

        // When
        final ConditionSpec conditionSpec = joinSpec.using("TEST_COLUMN");
        final List<ConditionSpec> result = joinSpec.conditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
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
        when(protoExpressionResolver.resolveExpression(any(ExpressionSpec.class))).thenAnswer(i -> Stream.of((ExpressionSpec) i.getArgument(0)));
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver));
        final Table table = joinSpec.table();
        final ConditionSpec conditionSpec = joinSpec.newCondition(new Column(table, "TEST_COLUMN"));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);

        // When
        final Join result = joinSpec.toJoin();

        // Then
        assertNotNull(result);
        assertSame(table, result.table());
        assertNotNull(result.conditions());
        assertEquals(1, result.conditions().size());
        assertEquals(conditionSpec.toCondition(new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver), Collections.singletonList(table)),
                result.conditions().getFirst());
    }
}