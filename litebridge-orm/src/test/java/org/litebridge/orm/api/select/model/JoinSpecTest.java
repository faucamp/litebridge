package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.LiteralExpressionFactory;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.sql.SqlJoinSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.expression.TestSelectReferenceExpressionFactory;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(tableMetaDataCache.ensureTableMetaData(any(Table.class))).thenReturn(tableMetaData);
        when(tableMetaData.column(anyString())).thenReturn(columnMetaData);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);

        // When
        final ConditionSpec conditionSpec = joinSpec.using("TEST_COLUMN");
        final LogicCondition result = joinSpec.toJoin(Collections.emptyList(), new ArrayList<>(), tableMetaDataCache, mock(TypeConverter.class)).conditions().conditions().getFirst();

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
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("TEST_SCHEMA", "TEST_TABLE"), new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver, mock(TableMetaDataCache.class), new DefaultTypeConverter()));
        final Table table = joinSpec.table();
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(new Column(table, "TEST_COLUMN")));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);
        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(tableMetaDataCache.ensureTableMetaData(any(Table.class))).thenReturn(tableMetaData);
        when(tableMetaData.column(anyString())).thenReturn(columnMetaData);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);

        // When
        final Join result = joinSpec.toJoin(Collections.emptyList(), new ArrayList<>(), tableMetaDataCache, new DefaultTypeConverter());

        // Then
        assertNotNull(result);
        assertSame(table, result.table());
        assertNotNull(result.conditions());
        assertFalse(result.conditions().isEmpty());
    }
}