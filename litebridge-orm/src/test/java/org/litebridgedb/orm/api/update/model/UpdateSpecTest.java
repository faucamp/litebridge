package org.litebridgedb.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.ProtoExpressionResolver;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;
import org.litebridgedb.orm.expression.TestSelectReferenceExpressionFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateSpecTest {

    @Test
    void updateSpec() {
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        final SqlFunctionRegistry.Select selectRegistry = mock(SqlFunctionRegistry.Select.class);
        when(sqlFunctionRegistry.select()).thenReturn(selectRegistry);
        when(selectRegistry.column()).thenReturn(new TestColumnExpressionFactory());
        when(selectRegistry.reference()).thenReturn(new TestSelectReferenceExpressionFactory());
        when(selectRegistry.literal()).thenReturn(LiteralExpression::new);

        final ProtoExpressionResolver protoExpressionResolver = mock(ProtoExpressionResolver.class);
        when(protoExpressionResolver.resolveExpression(any(ExpressionSpec.class))).thenAnswer(i -> Stream.of((ExpressionSpec) i.getArgument(0)));
        final UpdateSpec spec = new UpdateSpec(new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver));
        final Table table = new Table("cat", "sch", "tab");
        spec.setTable(table);
        assertEquals(table, spec.getTable());

        Column col = new Column(table, "col");
        spec.addColumnValue(new ColumnValue(col, "val"));
        ConditionSpec conditionSpec = spec.newWhereCondition(col);
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");

        assertNotNull(spec.getWhereConditions());
        assertEquals(1, spec.getWhereConditions().size());

        Update update = spec.toUpdate();
        assertEquals(table, update.table());
        assertEquals(1, update.columnValues().size());
        assertEquals(1, update.where().size());

        spec.setWhereConditions(null);
        assertNull(spec.getWhereConditions());
    }
}