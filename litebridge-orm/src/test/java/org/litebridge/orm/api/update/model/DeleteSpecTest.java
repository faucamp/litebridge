package org.litebridge.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.mockito.Mockito.mock;

class DeleteSpecTest {

    @Test
    void deleteSpec() {
        final Table table = new Table("cat", "sch", "tab");
        final DeleteSpec spec = new DeleteSpec(table, mock(SelectExpressionMapper.class));

        final Column col = new Column(table, "col");
        final ConditionGroupSpec conditionGroupSpec = spec.pushConditionGroupSpec(LogicOperator.OR);
        final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(LogicOperator.NOOP, new SelectColumnSpec(col));
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");
    }
}
