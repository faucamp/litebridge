package org.litebridge.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.TestColumnExpressionFactory;
import org.litebridge.orm.expression.TestSelectReferenceExpressionFactory;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(protoExpressionResolver.resolveExpression(any(ExpressionSpec.class), any(ClauseType.class))).thenAnswer(i -> Stream.of((ExpressionSpec) i.getArgument(0)));
        final Table table = new Table("cat", "sch", "tab");
        final UpdateSpec spec = new UpdateSpec(table, new SelectExpressionMapper(sqlFunctionRegistry, protoExpressionResolver));

        final Column col = new Column(table, "col");
        spec.addColumnValue(new ColumnValue(col, "val"));
        final ConditionSpec conditionSpec = spec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(col));
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");

        final Update update = spec.toUpdate();
        assertEquals(table, update.table());
        assertEquals(1, update.columnValues().size());
        assertEquals(1, update.where().conditions().size());
    }
}