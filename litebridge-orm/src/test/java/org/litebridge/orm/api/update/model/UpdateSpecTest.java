package org.litebridge.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.expression.TestSelectReferenceExpressionFactory;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.sql.Types;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateSpecTest {

    @Test
    void updateSpec() {
        // Given
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);

        final Table table = new Table("cat", "sch", "tab");
        final ProtoExpressionResolver protoExpressionResolver = mock(ProtoExpressionResolver.class);
        when(protoExpressionResolver.resolveExpression(any(ExpressionSpec.class), table, any(ClauseType.class))).thenAnswer(i -> Stream.of((ExpressionSpec) i.getArgument(0)));
        final UpdateSpec spec = new UpdateSpec(table, new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver, mock(TableMetaDataCache.class), new DefaultTypeConverter()));

        final Column col = new Column(table, "col");
        spec.addColumnValue(new ColumnValue(col, "val"));
        final ConditionSpec conditionSpec = spec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(col));
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");

        final TableMetaDataCache tableMetaDataCache = mock(TableMetaDataCache.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(tableMetaDataCache.ensureTableMetaData(any(Table.class))).thenReturn(tableMetaData);
        when(tableMetaData.column(anyString())).thenReturn(columnMetaData);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);

        // When
        final PreparedOperation preparedOperation = spec.toUpdate(tableMetaDataCache, new DefaultTypeConverter());

        // Then
        final Update update = (Update) preparedOperation.operation();
        assertEquals(table, update.table());
        assertEquals(1, update.columnValues().size());
        assertEquals(1, update.where().conditions().size());
    }
}