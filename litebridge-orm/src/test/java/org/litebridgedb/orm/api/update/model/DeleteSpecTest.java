package org.litebridgedb.orm.api.update.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class DeleteSpecTest {

    @Test
    void deleteSpec() {
        final DeleteSpec spec = new DeleteSpec(mock(SelectExpressionMapper.class));
        final Table table = new Table("cat", "sch", "tab");
        spec.setTable(table);
        assertEquals(table, spec.getTable());

        Column col = new Column(table, "col");
        ConditionSpec conditionSpec = spec.newWhereCondition(col);
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue("test");

        assertNotNull(spec.getWhereConditions());

        spec.setWhereConditions(null);
        assertNull(spec.getWhereConditions());

        spec.setTable(null);
        assertThrows(IllegalStateException.class, spec::getTable);
    }
}
